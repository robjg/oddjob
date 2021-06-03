/*
 * (c) Rob Gordon 2018
 */
package org.oddjob.events;

import org.oddjob.FailedToStopException;
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
	private volatile EventOf<T> trigger;

	private volatile transient Restore restore;
	
	public EventJobBase() {
		completeConstruction();
	}
	
	/**
	 * Called once following construction or deserialisation.
	 */
	private void completeConstruction() {
		asyncSupport = 
				new AsyncExecutionSupport(new Runnable() {
					public void run() {
						stop = false;
						try {
							save();
							EventJobBase.super.startChildStateReflector();
						} catch (ComponentPersistException e) {
							stateHandler().waitToWhen(s -> true, 
									() -> getStateChanger().setStateException(e));
							onStop();
						}
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

		final Object[] children = childHelper.getChildren();
		
		if (children.length < 1) {
			throw new IllegalArgumentException("No When Node.");
		}
		
		asyncSupport.reset();

		@SuppressWarnings("unchecked")
		final EventSource<T> eventSource = (EventSource<T>) children[0];
		
		final Object job;
		if (children.length > 1) {
			job = children[1];
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
		
		final AtomicReference<Consumer<? super EventOf<T>>> consumer = new AtomicReference<>();
		consumer.set(
				event -> {
					try (Restore ignored = ComponentBoundary.push(loggerName(), EventJobBase.this)) {
						logger().debug("Received immediate event [{}]", event);
						onImmediateEvent(event);
					}
				});

		logger().info("Starting event source [{}]", eventSource);
		Restore close = eventSource.start(event -> consumer.get().accept(event));

		if (job == null) {
			EventJobBase.super.startChildStateReflector();
		}
		else {
			stateHandler().waitToWhen(new IsStoppable(), 
					() -> getStateChanger().setState(ParentState.ACTIVE));
		}

		consumer.set(
				event -> {
					try (Restore ignored = ComponentBoundary.push(loggerName(), EventJobBase.this)) {
						logger().debug("Received event [{}]", event);
						stopChildStateReflector();
						onLaterEvent(event, job, executor);
						if (job == null) {
							super.startChildStateReflector();
						}
					}
				});
		
		restore = () -> {
			logger().info("Closing event source [{}]", eventSource);
			close.close();
			restore = null;
		};

		logger().info("Subscription to event source [{}] started.", eventSource);
		onSubscriptionStarted(job, executor);
	}

	abstract void onImmediateEvent(EventOf<T> event);

	abstract void onSubscriptionStarted(Object job, Executor executor);
	
	abstract void onLaterEvent(EventOf<T> event, Object job, Executor executor);

	
	@Override
	protected void onStop() {

		asyncSupport.cancelAllPendingJobs();
		Optional.ofNullable(restore).ifPresent(Restore::close);		
	}
	
	@Override
	protected void postStop() throws FailedToStopException {
		super.startChildStateReflector();
	}
					
	protected void setTrigger(EventOf<T> trigger) {
		this.trigger = trigger;
	}
	
	public EventOf<T> getTrigger() {
		return trigger;
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
