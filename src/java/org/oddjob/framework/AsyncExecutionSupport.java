package org.oddjob.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Helper class for things that execute jobs in parallel. This class
 * ensures that all simultaneous executions are tracked, can be stopped,
 * can be waited for and allows an action to be run on completion of 
 * all the simultaneous jobs. 
 * 
 * 
 * @see SimultaneousStructural
 * 
 * @author rob
 *
 */
public class AsyncExecutionSupport {
	
	/** The job threads. */
	private final List<Future<?>> futures = Collections.synchronizedList(
			new ArrayList<Future<?>>());
	
	/** Watch execution to start the state reflector when all children
	 * have finished. */
	private final ExecutionWatcher executionWatcher;

	/**
	 * Create a new instance.
	 * 
	 * @param onCompleteAction
	 */
	public AsyncExecutionSupport(Runnable onCompleteAction) {
		executionWatcher = new ExecutionWatcher(onCompleteAction);
	}
	
	public void submitJob(ExecutorService executorService, Runnable job) {
		
		Future<?> future = executorService.submit(
				executionWatcher.addJob(job));
		futures.add(future);		
	}
	
	public void joinOnAllJobs() throws InterruptedException, ExecutionException {
		for (Future<?> future : futures) {
			future.get();
		}
	}
	
	/**
	 * Start watching jobs for them to finish executing.
	 */
	public void startWatchingJobs() {
		executionWatcher.start();
	}
	
	/**
	 * Cancel all pending jobs. This will not stop jobs already executing
	 * but will cancel pending jobs. Stopping jobs is left to calling
	 * code.
	 * <p>
	 * Also stop watching executing jobs. 
	 */
	public void cancelAllPendingJobs() {
		
		for (Future<?> future : futures) {
			future.cancel(false);
		}
		
		executionWatcher.stop();
	}
	
	/**
	 * Reset the internal state so that it can be used again with
	 * a new set of jobs.
	 */
	public void reset() {
		
		executionWatcher.reset();
		futures.clear();
	}

	/**
	 * The number of simultaneous job currently being tracked.
	 * 
	 * @return
	 */
	public int size() {
		return futures.size();
	}
}
