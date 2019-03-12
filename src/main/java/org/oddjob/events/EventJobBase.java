/*
 * (c) Rob Gordon 2018
 */
package org.oddjob.events;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.inject.Inject;

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

	private volatile EventOf<T> current;

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
			close.close();
			restore = null;
			
		};
		
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
					
	protected void setCurrent(EventOf<T> current) {
		this.current = current;
	}
	
	public EventOf<T> getCurrent() {
		return current;
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
	
	protected static class ExecutionContext<T> {
		
		private final EventSource<T> eventSource;
		
		private final Optional<Object> job;
		
		private final ExecutorService executorService;
		
		protected ExecutionContext(EventSource<T> eventSource,
									Object job,
									ExecutorService executorService) {
			Objects.requireNonNull(eventSource, "Ann Event Source must be provided.");
			Objects.requireNonNull(executorService, "An Executor Service must be provided.");
			
			this.eventSource = eventSource;
			this.job = Optional.ofNullable(job);
			this.executorService = executorService;
		}

		public EventSource<T> getEventSource() {
			return eventSource;
		}

		public Optional<Object> getJob() {
			return job;
		}

		public ExecutorService getExecutorService() {
			return executorService;
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
