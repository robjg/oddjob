/*
 * (c) Rob Gordon 2018
 */
package org.oddjob.events;

import org.oddjob.Stateful;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.Outbound;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.state.*;
import org.oddjob.util.Restore;

import javax.inject.Inject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Base class for Jobs that react to events.
 *
 * @param <T> The type of the event or trigger.
 *
 * @author Rob Gordon.
 */
abstract public class EventJobBase<T> extends StructuralJob<Object> implements Consumer<T>, Outbound<T> {
	
	private static final long serialVersionUID = 2018060600L; 
	
	/** Unlike other structurals, this is only used to cancel outstanding tasks.  */
	private volatile transient Future<?> asyncSupport;
		
	/** The scheduler to schedule on. */
	private volatile transient ExecutorService executorService;

	/**
	 * @oddjob.property
	 * @oddjob.description The trigger event.
	 * @oddjob.required Read only.
	 */
	private volatile T trigger;

	private volatile boolean beDestination;

	/**
	 * @oddjob.property
	 * @oddjob.description The source of events. If this is not set the first child component is assumed
	 * to be the Event Source.
	 * @oddjob.required No.
	 */
	private volatile EventSource<T> eventSource;

	/**
	 * @oddjob.property
	 * @oddjob.description Provide the event to a Bean Bus style consumer.
	 * @oddjob.required No.
	 */
	private volatile Consumer<? super T> to;

	/** Used to unsubscribe on stop */
	private final AtomicReference<Restore> unsubscribe = new AtomicReference<>();

	private final AtomicReference<Runnable> removeListener = new AtomicReference<>();

	public EventJobBase() {
		completeConstruction();
	}
	
	/**
	 * Called once following construction or deserialisation.
	 */
	private void completeConstruction() {
	}

	static class ConsumerEventSource<T> implements EventSource<T> {

		private volatile Consumer<? super T> subscribed;

		@Override
		public Restore subscribe(Consumer<? super T> consumer) {
			this.subscribed = consumer;
			return () -> subscribed = null;
		}

		@Override
		public String toString() {
			return "ConsumerEventSource";
		}
	}

	@ArooaHidden
	@Inject
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
	
	@Override
	protected StateOperator getInitialStateOp() {
		return new AnyActiveStateOp();
	}
	
	@Override
	protected void startChildStateReflector() {
		// we start this ourselves
	}
	
	@Override
	protected void execute() throws Throwable {

		final Object[] children = childHelper.getChildren();

		// We can either take the event source as the first child
		// or as the property.
		final EventSource<T> eventSource;
		int jobIndex;

		if (this.beDestination) {
			eventSource = new ConsumerEventSource<>();
			this.eventSource = eventSource;
			jobIndex = 0;
		}
		else {
			if (this.eventSource == null) {
				if (children.length == 0) {
					throw new IllegalStateException(
							"No Event Source provided either as a property or a child component.");
				}
				Object firstChild = children[0];

				eventSource = EventSourceAdaptor.<T>maybeEventSourceFrom(firstChild, getArooaSession())
						.orElseThrow(() -> new IllegalStateException("" +
								"When Event Source provided as a property, " +
								"the first child component is expected to be an Event Source."));

				jobIndex = 1;
			} else {
				eventSource = this.eventSource;
				jobIndex = 0;
			}
		}

		final Object job;
		if (children.length > jobIndex) {
			job = children[jobIndex];
		}
		else {
			throw new IllegalStateException("A Job to run on receiving the event must be provided.");
		}

		if (job instanceof Stateful) {
			StateListener stateListener = stateOnChildComplete();
			((Stateful) job).addStateListener(stateListener);
			removeListener.set(() -> {
				((Stateful) job).removeStateListener(stateListener);
				removeListener.set(null);
			} );
		}

		Executor executor = j -> {
			try (Restore ignored = ComponentBoundary.push(loggerName(), EventJobBase.this)) {
				stateHandler().runLocked(() -> getStateChanger().setState(ParentState.ACTIVE));

				EventJobBase.this.asyncSupport = executorService.submit(j);
				logger().info("Submitted [" + j + "]");				
			}
		};

		ConsumerSwitch consumerSwitch = new ConsumerSwitch();

		logger().info("Starting event source [{}]", eventSource);
		Restore close = eventSource.subscribe(consumerSwitch);

		if (job == null) {
			EventJobBase.super.startChildStateReflector();
		}
		else {
			stateHandler().waitToWhen(new IsStoppable(), 
					() -> getStateChanger().setState(ParentState.ACTIVE));
		}

		consumerSwitch.makeSwitch(job, executor);

		unsubscribe.set(() -> {
			logger().info("Closing event source [{}]", eventSource);
			close.close();
			unsubscribe.set(null);
		});

		// unlikely but stop might have happened during startup.
		if (stop) {
			unsubscribe();
			switchToChildStateReflector();
		}
		else {
			logger().info("Subscription to event source [{}] started.", eventSource);
			onSubscriptionStarted(job, executor);
		}
	}

	class ConsumerSwitch implements Consumer<T> {

		final AtomicReference<Consumer<? super T>> consumer;

		ConsumerSwitch() {
			this.consumer = new AtomicReference<>(new ImmediateEventHandler());
		}

		@Override
		public void accept(T t) {
			this.consumer.get().accept(t);
			Optional.ofNullable(to).ifPresent(next -> next.accept(t));
		}

		void makeSwitch(Object job, Executor executor) {
			this.consumer.set(new LaterEventHandler(job, executor));
		}

		@Override
		public String toString() {
			return "EventHandler of " + EventJobBase.this;
		}
	}

	class ImmediateEventHandler implements Consumer<T> {

		@Override
		public void accept(T event) {
			try (Restore ignored = ComponentBoundary.push(loggerName(), EventJobBase.this)) {
				logger().debug("Received immediate event [{}]", event);
				onImmediateEvent(event);
			}
		}

		@Override
		public String toString() {
			return "ImmediateEventHandler of " + EventJobBase.this;
		}
	}

	class LaterEventHandler implements Consumer<T> {

		private final Object job;

		private final Executor executor;

		LaterEventHandler(Object job, Executor executor) {
			this.job = job;
			this.executor = executor;
		}

		@Override
		public void accept(T event) {
			try (Restore ignored = ComponentBoundary.push(loggerName(), EventJobBase.this)) {
				logger().debug("Received event [{}]", event);
				stopChildStateReflector();
				onLaterEvent(event, job, executor);
				if (job == null) {
					EventJobBase.super.startChildStateReflector();
				}
			}
		}

		@Override
		public String toString() {
			return "EventHandler of " + EventJobBase.this;
		}
	}

	/**
	 * Do something with an event received immediately before the subscribe method has returned.
	 *
	 * @param event The event.
	 */
	abstract protected void onImmediateEvent(T event);

	/**
	 * Called once the subscription has started.
	 *
	 * @param job The job to execute if any.
	 * @param executor The executor to use to execute the job.
	 */
	abstract protected void onSubscriptionStarted(Object job, Executor executor);

	/**
	 * Called when an event is received after subscription. There is no guarantee that an
	 * this will be called after the {@link #onSubscriptionStarted(Object, Executor)} method,
	 * implementors are responsible for their own synchronisation if required.
	 *
	 * @param event The event.
	 */
	abstract protected void onLaterEvent(T event, Object job, Executor executor);

	abstract protected StateListener stateOnChildComplete();

	protected void unsubscribe() {
		Optional.ofNullable(this.unsubscribe.get()).ifPresent(Restore::close);
	}

	protected void switchToChildStateReflector() {

		Optional.ofNullable(this.removeListener.get()).ifPresent(Runnable::run);
		super.startChildStateReflector();
	}

	@Override
	protected void onStop() {

		unsubscribe();
		Optional.ofNullable(this.asyncSupport).ifPresent(fut -> fut.cancel(false));
	}
	
	@Override
	protected void postStop() {
		switchToChildStateReflector();
	}

	@Override
	public void accept(T t) {
		Consumer<? super T> subscribe = Optional.ofNullable(this.eventSource)
				.map(es -> {
					if (! (es instanceof ConsumerEventSource)) {
						throw new IllegalStateException(
								"Bus operation not supported - using alternative event source" + EventJobBase.this);
					}
					ConsumerEventSource<T> ces = (ConsumerEventSource<T>) es;

					return Optional.ofNullable(ces.subscribed)
							.orElseThrow(() -> new IllegalStateException(
									"Bus operation not supported - not subscribed" + EventJobBase.this));
				})
				.orElseThrow(() -> new IllegalStateException(
						"Bus operation not support - not started " + EventJobBase.this));
		subscribe.accept(t);
	}

	public boolean isBeDestination() {
		return beDestination;
	}

	public void setBeDestination(boolean beDestination) {
		this.beDestination = beDestination;
	}

	@Override
	public void setTo(Consumer<? super T> destination) {
		this.to = destination;
	}

	public Consumer<? super T> getTo() {
		return to;
	}

	protected void setTrigger(T trigger) {
		this.trigger = trigger;
	}
	
	public T getTrigger() {
		return trigger;
	}

	public EventSource<T> getEventSource() {
		return eventSource;
	}

	public void setEventSource(EventSource<T> eventSource) {
		this.eventSource = eventSource;
	}

	/**
	 * Add a child job.
	 * 
	 * @oddjob.property jobs
	 * @oddjob.description The child jobs.
	 * @oddjob.required No, but pointless if missing.
	 * 
	 * @param child A child
	 */
	@ArooaComponent
	public void setJobs(int index, Object child) {
	    logger().debug(
	    		"Adding child [" + 
	    		child + "], index [" + 
	    		index + "]");
	    
		if (child == null) {
			childHelper.removeChildAt(index);
		}
		else {
			childHelper.insertChild(index, child);
		}
	}
	
	/*
	 * Custom serialization.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
	}
	
	/*
	 * Custom serialization.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		completeConstruction();
	}
}
