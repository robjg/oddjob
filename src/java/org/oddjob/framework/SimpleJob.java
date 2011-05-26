package org.oddjob.framework;


import org.oddjob.FailedToStopException;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.images.IconHelper;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;

/**
 * An abstract implementation of a job which provides common functionality to
 * concrete sub classes.
 * 
 * @author Rob Gordon
 */
public abstract class SimpleJob extends BasePrimary
implements  Runnable, Resetable, Stateful {

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
		OddjobNDC.push(loggerName());
		try {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					getStateChanger().setJobState(JobState.EXECUTING);
				}
			})) {
				return;			
			}
			
			logger().info("[" + SimpleJob.this + "] Executing.");

			final int[] result = new int[1];
			final Throwable[] exception = new Throwable[1];
			
			try {			
				configure();
				
				result[0] = execute();
				
				logger().info("[" + SimpleJob.this + "] Finished, result " + 
						result[0]);
			}
			catch (Throwable e) {
				logger().error("[" + SimpleJob.this + "] Exception executing job.", e);
				exception[0] = e;
			}
			
			stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					if (exception[0] != null) {
						getStateChanger().setJobStateException(exception[0]);
					}
					else if (result[0] == 0) {
		            	getStateChanger().setJobState(JobState.COMPLETE);
					}
					else {						
		            	getStateChanger().setJobState(JobState.INCOMPLETE);
					}
				}
			});
		}
		finally {
			OddjobNDC.pop();
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

		if (!stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
			public void run() {
				stop = true;
				stateHandler.wake();
			}
		})) {
			return;
		}
		
		logger().info("[" + this + "] Stop requested.");
		
		iconHelper.changeIcon(IconHelper.STOPPING);
		try {
			onStop();
			
			synchronized (this) {
				notifyAll();
			}
			
			new StopWait(this).run();
			
			logger().info("[" + this + "] Stopped.");
			
		} catch (RuntimeException e) {
			iconHelper.changeIcon(IconHelper.EXECUTING);
			throw e;
		}
		catch (FailedToStopException e) {
			iconHelper.changeIcon(IconHelper.EXECUTING);
			throw e;
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
		return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
			public void run() {
				onReset();
				
				getStateChanger().setJobState(JobState.READY);
				
				logger().info("[" + SimpleJob.this + "] Soft Reset.");
			}
		});
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
			public void run() {
				onReset();
				
				getStateChanger().setJobState(JobState.READY);
		
				logger().info("[" + SimpleJob.this + "] Hard Reset.");
			}
		});
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
}
