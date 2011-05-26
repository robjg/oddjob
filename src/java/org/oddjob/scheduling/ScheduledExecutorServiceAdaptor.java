package org.oddjob.scheduling;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ScheduledExecutorServiceAdaptor implements ScheduledExecutorService {

	private final ScheduledExecutorService delegate; 
	
	public ScheduledExecutorServiceAdaptor(ScheduledExecutorService delegate) {
		this.delegate = delegate;
	}
	
	public ScheduledFuture<?> schedule(Runnable command, long delay,
			TimeUnit unit) {
		return delegate.schedule(command, delay, unit);
	}

	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
			TimeUnit unit) {
		return delegate.schedule(callable, delay, unit);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {
		return delegate.scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit) {
		return delegate.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return delegate.awaitTermination(timeout, unit);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		return delegate.invokeAll(tasks);
	}

	public <T> List<Future<T>> invokeAll(
			Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return delegate.invokeAll(tasks, timeout, unit);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return delegate.invokeAny(tasks);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return delegate.invokeAny(tasks, timeout, unit);
	}

	public boolean isShutdown() {
		return delegate.isShutdown();
	}

	public boolean isTerminated() {
		return delegate.isTerminated();
	}

	public void shutdown() {
		delegate.shutdown();
	}

	public List<Runnable> shutdownNow() {
		return delegate.shutdownNow();
	}

	public <T> Future<T> submit(Callable<T> task) {
		return delegate.submit(task);
	}

	public Future<?> submit(Runnable task) {
		return delegate.submit(task);
	}

	public <T> Future<T> submit(Runnable task, T result) {
		return delegate.submit(task, result);
	}

	public void execute(Runnable command) {
		delegate.execute(command);
	}

}
