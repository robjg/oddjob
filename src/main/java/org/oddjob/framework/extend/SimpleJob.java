package org.oddjob.framework.extend;


import org.oddjob.FailedToStopException;
import org.oddjob.Forceable;
import org.oddjob.Resettable;
import org.oddjob.Stateful;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.framework.util.StopWait;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.state.*;
import org.oddjob.util.Restore;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An abstract implementation of a job which provides common functionality to
 * concrete sub classes.
 * 
 * @author Rob Gordon
 */
public abstract class SimpleJob extends BasePrimary
implements  Runnable, Resettable, Stateful, Forceable {

	/** Handle state. */
	private final JobStateHandler stateHandler;
	
	/** Used to notify clients of an icon change. */
	private final IconHelper iconHelper;
	
	/** Perform the state change. */
	private final JobStateChanger stateChanger;
	
	/**
	 * @oddjob.property
	 * @oddjob.description This flag is set by the stop method and should
	 * be examined by any Stoppable jobs in their processing loops.
	 * @oddjob.required Read Only.
	 */
	protected transient volatile boolean stop;
	
	protected SimpleJob() {
		stateHandler = new JobStateHandler(this);
		iconHelper = new IconHelper(this, 
				StateIcons.iconFor(stateHandler.getState()));
		stateChanger = new JobStateChanger(stateHandler, iconHelper,
				this::save);
	}
	
	@Override
	protected JobStateHandler stateHandler() {
		return stateHandler;
	}
	
	@Override
	protected IconHelper iconHelper() {
		return iconHelper;
	}
	
	protected StateChanger<JobState> getStateChanger() {
		return stateChanger;
	}
	
	/**
	 * Execute this job.
	 * 
	 * @return 0 if the job is complete, anything else otherwise.
	 * @throws Exception If the unexpected occurs.
	 */
	abstract protected int execute() throws Throwable;

	/**
	 * Implement the main execute method for a job. This surrounds the 
	 * doExecute method of the sub class and sets state for the job.
	 */
	public final void run() {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			if (!stateHandler.waitToWhen(new IsExecutable(),
					() -> getStateChanger().setState(JobState.EXECUTING))) {
				return;			
			}
			
			logger().info("Executing.");

			final AtomicInteger result = new AtomicInteger();
			final AtomicReference<Throwable> exception =
					new AtomicReference<>();
			
			try {			
				configure();
				
				result.set(execute());
				
				logger().info("Finished, result " + 
						result.get());
			}
			catch (Throwable e) {
				logger().error("Exception executing job.", e);
				exception.set(e);
			}
			
			stateHandler.waitToWhen(new IsStoppable(), () -> {
				if (exception.get() != null) {
					getStateChanger().setStateException(exception.get());
				}
				else if (result.get() == 0) {
					getStateChanger().setState(JobState.COMPLETE);
				}
				else {
					getStateChanger().setState(JobState.INCOMPLETE);
				}
			});
		}
	}
	
	/**
	 * Utility method to sleep a certain time.
	 * 
	 * @param waitTime Milliseconds to sleep for.
	 */
	protected void sleep(final long waitTime) {
		stateHandler().assertAlive();
		
		if (!stateHandler().waitToWhen(new IsStoppable(), () -> {
			if (stop) {
				logger().debug("Stop request detected. Not sleeping.");

				return;
			}

			logger().debug("Sleeping for " + (
					waitTime == 0 ? "ever" : "[" + waitTime + "] milli seconds") + ".");

			iconHelper.changeIcon(IconHelper.SLEEPING);

			try {
				stateHandler().sleep(waitTime);
			} catch (InterruptedException e) {
				logger().debug("Sleep interrupted.");
			}

			// Stop should already have set Icon to Stopping.
			if (!stop) {
				iconHelper.changeIcon(IconHelper.EXECUTING);
			}
		})) {
			throw new IllegalStateException("Can't sleep unless EXECUTING.");
		}
	}	
		
	/**
	 * Allow subclasses to indicate they are 
	 * stopping. The subclass must still implement 
	 * Stoppable.
	 * 
	 * @throws FailedToStopException 
	 */
	public final void stop() throws FailedToStopException {
		stateHandler.assertAlive();

		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			if (!stateHandler.waitToWhen(new IsStoppable(), () -> {
				logger().info("Stopping.");

				stop = true;
				stateHandler.wake();
				iconHelper.changeIcon(IconHelper.STOPPING);
			})) {
				logger().debug("Stop request ignored as not running.");
				return;
			}
			
	    	FailedToStopException failedToStopException = null;
	    	
			try {
				onStop();
				
			} catch (RuntimeException e) {
				failedToStopException = new FailedToStopException(this, e);
			}
			catch (FailedToStopException e) {
				failedToStopException = e;
			}
			
			if (failedToStopException != null) {
				
				stateHandler().waitToWhen(new IsStoppable(),
						() -> iconHelper.changeIcon(IconHelper.EXECUTING));
				
				throw failedToStopException;
			}
		} 
	}
	
	/**
	 * Allow sub classes to do something on stop.
	 */
	protected void onStop() throws FailedToStopException { }
	
	/**
	 * Getter for stop flag.
	 * 
	 * @return
	 */
	public boolean isStop() {
		return stop;
	}
	
	/**
	 * Perform a soft reset on the job.
	 */
	@Override
	public boolean softReset() {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			return stateHandler.waitToWhen(new IsSoftResetable(), () -> {
				onReset();

				getStateChanger().setState(JobState.READY);

				stop = false;

				logger().info("Soft Reset complete.");
			});
		}
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	@Override
	public boolean hardReset() {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			return stateHandler.waitToWhen(new IsHardResetable(), () -> {
				onReset();

				getStateChanger().setState(JobState.READY);

				stop = false;

				logger().info("Hard Reset complete.");
			});
		}
	}

 	/**
	 * Allow sub classes to do something on reset.
	 */
	protected void onReset() {
		
	}

	/**
	 * Force the job to COMPLETE.
	 */
	@Override
	public void force() {
		
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			stateHandler.waitToWhen(new IsForceable(), () -> {
				logger().info("Forcing complete.");

				getStateChanger().setState(JobState.COMPLETE);
			});
		} 
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		try {
			stop();
			new StopWait(this).run();
		} catch (FailedToStopException e) {
			logger().warn("Failed to stop.", e);
		}
	}
	
	/**
	 * Internal method to fire state.
	 */
	protected void fireDestroyedState() {
		
		if (!stateHandler().waitToWhen(new IsAnyState(), () -> {
			stateHandler().setState(JobState.DESTROYED);
			stateHandler().fireEvent();
		})) {
			throw new IllegalStateException("[" + SimpleJob.this + "] Failed set state DESTROYED");
		}
		logger().debug("[" + this + "] Destroyed.");				
	}
}
