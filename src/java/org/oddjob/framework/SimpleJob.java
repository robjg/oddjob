package org.oddjob.framework;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.oddjob.FailedToStopException;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.images.IconHelper;
import org.oddjob.persist.Persistable;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateChanger;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.StateChanger;

/**
 * An abstract implementation of a job which provides common functionality to
 * concrete sub classes.
 * 
 * @author Rob Gordon
 */
public abstract class SimpleJob extends BasePrimary
implements  Runnable, Resetable, Stateful {

	protected transient JobStateHandler stateHandler;
	
	private final JobStateChanger stateChanger;
	
	/**
	 * This flag is set by the stop method and should
	 * be examined by any Stoppable sub classes in 
	 * their processing loop.
	 */
	protected transient volatile boolean stop;
	
	protected SimpleJob() {
		stateHandler = new JobStateHandler(this);
		stateChanger = new JobStateChanger(stateHandler, iconHelper, 
				new Persistable() {					
					@Override
					public void persist() throws ComponentPersistException {
						save();
					}
				});
	}
	
	@Override
	protected JobStateHandler stateHandler() {
		return stateHandler;
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
		ComponentBoundry.push(loggerName(), this);
		try {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					getStateChanger().setState(JobState.EXECUTING);
				}
			})) {
				return;			
			}
			
			logger().info("Executing.");

			final AtomicInteger result = new AtomicInteger();
			final AtomicReference<Throwable> exception = 
				new AtomicReference<Throwable>();
			
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
			
			stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					if (exception.get() != null) {
						getStateChanger().setStateException(exception.get());
					}
					else if (result.get() == 0) {
		            	getStateChanger().setState(JobState.COMPLETE);
					}
					else {						
		            	getStateChanger().setState(JobState.INCOMPLETE);
					}
				}
			});
		}
		finally {
			ComponentBoundry.pop();
		}
	}
	
	/**
	 * Utility method to sleep a certain time.
	 * 
	 * @param waitTime Milliseconds to sleep for.
	 */
	protected void sleep(final long waitTime) {
		stateHandler().assertAlive();
		
		if (!stateHandler().waitToWhen(new IsStoppable(), new Runnable() {
			public void run() {
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
					logger().debug("Sleep interupted.");
				}
				
				// Stop should already have set Icon to Stopping.
				if (!stop) {
					iconHelper.changeIcon(IconHelper.EXECUTING);
				}
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

		ComponentBoundry.push(loggerName(), this);
		try {
			if (!stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					stop = true;
					stateHandler.wake();
					iconHelper.changeIcon(IconHelper.STOPPING);
				}
			})) {
				return;
			}
			
			logger().info("Stopping.");
			
			try {
				onStop();
				
				synchronized (this) {
					notifyAll();
				}
				
				new StopWait(this).run();
				
				logger().info("Stopped.");
				
			} catch (RuntimeException e) {
				iconHelper.changeIcon(IconHelper.EXECUTING);
				throw e;
			}
			catch (FailedToStopException e) {
				iconHelper.changeIcon(IconHelper.EXECUTING);
				throw e;
			}
		} finally {
			ComponentBoundry.pop();
		}
	}
	
	/**
	 * Allow sub classes to do something on stop.
	 */
	protected void onStop() throws FailedToStopException { }
	
	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		ComponentBoundry.push(loggerName(), this);
		try {
			return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {
					onReset();
					
					getStateChanger().setState(JobState.READY);
					
					stop = false;
					
					logger().info("Soft Reset complete.");
				}
			});
		} finally {
			ComponentBoundry.pop();
		}
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		ComponentBoundry.push(loggerName(), this);
		try {
			return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
				public void run() {
					onReset();
					
					getStateChanger().setState(JobState.READY);
			
					stop = false;
					
					logger().info("Hard Reset complete.");
				}
			});
		} finally {
			ComponentBoundry.pop();
		}
	}

 	/**
	 * Allow sub classes to do something on reset.
	 */
	protected void onReset() {
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		try {
			stop();
		} catch (FailedToStopException e) {
			logger().warn(e);
		}
	}
	
	/**
	 * Internal method to fire state.
	 */
	protected void fireDestroyedState() {
		
		if (!stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stateHandler().setState(JobState.DESTROYED);
				stateHandler().fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + SimpleJob.this + "] Failed set state DESTROYED");
		}
		logger().debug("[" + this + "] Destroyed.");				
	}
}
