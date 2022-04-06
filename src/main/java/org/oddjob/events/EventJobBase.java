/*
 * (c) Rob Gordon 2018
 */
package org.oddjob.events;

import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.framework.util.AsyncExecutionSupport;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateOperator;
import org.oddjob.util.Restore;

import javax.inject.Inject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Base class for Jobs that react to events.
 *
 * @param <T> The type of the event or trigger.
 *
 * @author Rob Gordon.
 */
abstract public class EventJobBase<T> extends StructuralJob<Object> {
	
	private static final long serialVersionUID = 2018060600L; 
	
	/** Watch execution to start the state reflector when all children
	 * have finished, and track job threads.  */
	private volatile transient AsyncExecutionSupport asyncSupport;	
		
	/** The scheduler to schedule on. */
	private volatile transient ExecutorService executorService;

	/**
	 * @oddjob.property
	 * @oddjob.description The trigger event.
	 * @oddjob.required Read only.
	 */
	private volatile T trigger;

	private volatile transient Restore restore;

	/**
	 * @oddjob.property
	 * @oddjob.description The source of events. If this is not set the first child component is assumed
	 * to be the Event Source.
	 * @oddjob.required No.
	 */
	private volatile EventSource<T> eventSource;

	public EventJobBase() {
		completeConstruction();
	}
	
	/**
	 * Called once following construction or deserialisation.
	 */
	private void completeConstruction() {
		asyncSupport = 
				new AsyncExecutionSupport(() -> {
					stop = false;
					try {
						save();
						EventJobBase.super.startChildStateReflector();
					} catch (ComponentPersistException e) {
						stateHandler().waitToWhen(s -> true,
								() -> getStateChanger().setStateException(e));
						onStop();
					}
				});
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

		asyncSupport.reset();

		final Object[] children = childHelper.getChildren();

		// We can either take the event source as the first child
		// or as the property.
		final EventSource<T> eventSource;
		int jobIndex;
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
		}
		else {
			eventSource = this.eventSource;
			jobIndex = 0;
		}
		
		final Object job;
		if (children.length > jobIndex) {
			job = children[jobIndex];
		}
		else {
			job = null;
		}
		
		Executor executor = j -> {
			try (Restore ignored = ComponentBoundary.push(loggerName(), EventJobBase.this)) {
				asyncSupport.submitJob(executorService, j);
				asyncSupport.startWatchingJobs();
				logger().info("Submitted [" + j + "]");				
			}
		};
		
		final AtomicReference<Consumer<? super T>> consumer = new AtomicReference<>();
		consumer.set(new ImmediateEventHandler());

		logger().info("Starting event source [{}]", eventSource);
		Restore close = eventSource.subscribe(event -> consumer.get().accept(event));

		if (job == null) {
			EventJobBase.super.startChildStateReflector();
		}
		else {
			stateHandler().waitToWhen(new IsStoppable(), 
					() -> getStateChanger().setState(ParentState.ACTIVE));
		}

		consumer.set(new LaterEventHandler(job, executor));
		
		restore = () -> {
			logger().info("Closing event source [{}]", eventSource);
			close.close();
			restore = null;
		};

		logger().info("Subscription to event source [{}] started.", eventSource);
		onSubscriptionStarted(job, executor);
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
	abstract void onImmediateEvent(T event);

	/**
	 * Called once the subscription has started.
	 *
	 * @param job The job to execute if any.
	 * @param executor The executor to use to execute the job.
	 */
	abstract void onSubscriptionStarted(Object job, Executor executor);

	/**
	 * Called when an event is received after subscription. There is no guarantee that an
	 * this will be called after the {@link #onSubscriptionStarted(Object, Executor)} method,
	 * implementors are responsible for their own synchronisation if required.
	 *
	 * @param event The event.
	 */
	abstract void onLaterEvent(T event, Object job, Executor executor);

	
	@Override
	protected void onStop() {

		asyncSupport.cancelAllPendingJobs();
		Optional.ofNullable(restore).ifPresent(Restore::close);		
	}
	
	@Override
	protected void postStop() {
		super.startChildStateReflector();
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
