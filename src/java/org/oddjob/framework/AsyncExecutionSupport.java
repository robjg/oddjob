package org.oddjob.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AsyncExecutionSupport {
	
	/** The job threads. */
	private final List<Future<?>> futures = Collections.synchronizedList(
			new ArrayList<Future<?>>());
	
	/** Watch execution to start the state reflector when all children
	 * have finished. */
	private final ExecutionWatcher executionWatcher;

	public AsyncExecutionSupport(Runnable onCompleteAction) {
		executionWatcher = new ExecutionWatcher(onCompleteAction);
	}
	
	public void reset() {
		
		executionWatcher.reset();
		futures.clear();
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
	
	public void startWatchingJobs() {
		executionWatcher.start();
	}
	
	public void stopAllJobs() {
		
		for (Future<?> future : futures) {
			future.cancel(false);
		}
		
		executionWatcher.stop();
	}
	
}
