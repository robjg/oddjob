package org.oddjob.framework.util;

import org.oddjob.framework.extend.SimultaneousStructural;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.function.Consumer;

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
	
	/** The completable futures. Needs to be thread safe as clear may happen on a different thread
	 * to the adding. */
	private final Collection<CompletableFuture<?>> futures = new ConcurrentLinkedQueue<>();

	private final Runnable onCompleteAction;

	private final Consumer<? super Throwable> exceptionHandler;

	/**
	 * Create a new instance.
	 * 
	 * @param onCompleteAction
	 */
	public AsyncExecutionSupport(Runnable onCompleteAction, Consumer<? super Throwable> exceptionHandler) {
		this.onCompleteAction = onCompleteAction;
		this.exceptionHandler = exceptionHandler;
	}
	
	public void submitJob(Executor executor, Runnable job) {
		
		futures.add(CompletableFuture.runAsync(job, executor));
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
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.whenComplete((v, t) -> {
					// We don't want to report cancellation exceptions because we want to run the
					// child state reflector as normal when cancelled.
					if (t == null || (t.getCause() != null && t.getCause() instanceof CancellationException)) {
						onCompleteAction.run();
					}
					else {
						exceptionHandler.accept(t);
					}
				});
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
	}
	
	/**
	 * Reset the internal state so that it can be used again with
	 * a new set of jobs.
	 */
	public void reset() {
		
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
