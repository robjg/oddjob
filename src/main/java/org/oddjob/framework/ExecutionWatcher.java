package org.oddjob.framework;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Watches the execution of jobs and executes an action when all jobs
 * have been executed.
 * <p>
 * When this watcher is stopped it will check to see if all jobs had
 * started executing and if they had it will perform the action. I can't 
 * remember why this this is required because the action is generally to 
 * start reflecting child state - and why don't we want to always want to
 * do that on stop?
 * 
 * @author rob
 *
 */
public class ExecutionWatcher {

	/** The action to run. */
	private final Runnable allJobsExecutedAction;

	/** The number to count to. */
	private final AtomicInteger jobsWatchingCount = new AtomicInteger(); 
	
	/** The number executing. */
	private final AtomicInteger jobsExecutingCount = new AtomicInteger();
	
	/** The number executed. */
	private final AtomicInteger jobsExecutedCount = new AtomicInteger();
	
	/** Started. */
	private volatile boolean started;
		
	/**
	 * Constructor.
	 * 
	 * @param action The action to run.
	 */
	public ExecutionWatcher(Runnable action) {
		this.allJobsExecutedAction = action;
	}

	/**
	 * Add a job.
	 * 
	 * @param job
	 * 
	 * @return The new job to execute.
	 */
	public Runnable addJob(final Runnable job) {
		
		jobsWatchingCount.incrementAndGet();
		
		return new Runnable() {
			
			@Override
			public void run() {
				jobsExecutingCount.incrementAndGet();
				job.run();
				boolean perform;
				synchronized (ExecutionWatcher.this) {
					jobsExecutingCount.decrementAndGet();
					jobsExecutedCount.incrementAndGet();
					perform = checkIfAllJobsExecuted();
				}
				
				if (perform) {
					allJobsExecutedAction.run();
				}
			}
			
			@Override
			public String toString() {
				return ExecutionWatcher.class.getSimpleName() + 
						" for " + job;
			}
		};
		
	}

	/**
	 * Starts the check.
	 */
	public void start() {

		boolean perform;
		synchronized (this) {
			started = true;
			perform = checkIfAllJobsExecuted();
		}
		if (perform) {
			allJobsExecutedAction.run();
		}
	}
	
	/**
	 * Stops the check.
	 */
	public void stop() {

		boolean performAllJobsExecutedAction;
		synchronized (this) {
			jobsWatchingCount.set(jobsExecutingCount.get() + jobsExecutedCount.get()); 
			performAllJobsExecutedAction = checkIfAllJobsExecuted();
		}
		
		if (performAllJobsExecutedAction) {
			allJobsExecutedAction.run();
		}
	}
	
	public void reset() {
		synchronized (this) {
			jobsWatchingCount.set(0);
			jobsExecutingCount.set(0);
			jobsExecutedCount.set(0);
			started = false;
		}
	}	
	
	/**
	 * Checks if all jobs have executed.
	 * 
	 * @return
	 */
	private boolean checkIfAllJobsExecuted() {
		if (started && jobsWatchingCount.get() == jobsExecutedCount.get()) {
			return true;
		}
		else {
			return false;
		}
	}	
}
