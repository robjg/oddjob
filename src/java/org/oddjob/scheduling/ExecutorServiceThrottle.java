package org.oddjob.scheduling;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@link ExecutorService} that limits the number of {@link Runnable}s 
 * running.
 * <p>
 * Work will be executed in the order in which it was submitted.
 * 
 * @author rob
 *
 */
public class ExecutorServiceThrottle extends AbstractExecutorService {

	/**
	 * The original {@link ExecutorService} that will actually do the 
	 * executing.
	 */
	private final ExecutorService executor;

	/**
	 * Outstanding work submitted to the executor
	 */
	private final LinkedList<Runnable> work = new LinkedList<Runnable>();
	
	/**
	 * The throttle limit.
	 */
	private final int threads;

	/**
	 * The number of currently running {@link Runnable}s.
	 */
	private final AtomicInteger count = new AtomicInteger();
	
	public ExecutorServiceThrottle(ExecutorService delegate, int threads) {
		this.executor = delegate;
		this.threads = threads;
	}
	
	
	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isShutdown() {
		return executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return executor.isTerminated();
	}

	@Override
	public void shutdown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Runnable> shutdownNow() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void execute(final Runnable command) {
		
		synchronized (work) {
			work.add(new Runnable() {
				@Override
				public void run() {
					try {
						command.run();
					}
					finally {
						count.decrementAndGet();
						submit();
					}
				}
			});
		}
		submit();
	}

	/**
	 * Submit any outstanding work if the number of {@link Runnable}s is less
	 * than the limit.
	 */
	private void submit() {
		
		synchronized (work) {
			if (work.isEmpty()) {
				return;
			}
			
			if (executor.isShutdown()) {
				work.clear();
				return;
			}
			
			if (count.get() < threads) {
				count.incrementAndGet();
				Runnable command = work.removeFirst();
				executor.execute(command);
			}
		}
	}
	
}
