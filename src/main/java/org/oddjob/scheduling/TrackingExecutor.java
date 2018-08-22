package org.oddjob.scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackingExecutor implements ScheduledExecutorService {
	static final Logger logger = LoggerFactory.getLogger(TrackingExecutor.class);

	private final ScheduledExecutorService executor;

	private final List<Wrapper> running = 
		new ArrayList<Wrapper>();
	
	public int getTaskCount() {
		synchronized (running) {
			return running.size();
		}
	}
	
	public void waitForNothingOutstanding() throws InterruptedException {
		synchronized (running) {
			while (running.size() > 0 && !executor.isShutdown()) {
				running.wait();
			}
		}
	}
	
	public TrackingExecutor(ScheduledExecutorService scheduler) {
		this.executor = scheduler;
	}
	
	public ScheduledFuture<?> schedule(Runnable command, long delay,
			TimeUnit unit) {
		RunnableWrapper wrapper = wrapperFor(command); 
		return scheduledFuture(
				executor.schedule(wrapper, delay, unit),
				wrapper);
	}

	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
			TimeUnit unit) {
		CallableWrapper<V> wrapper = wrapperFor(callable);
		return scheduledFuture(
				executor.schedule(wrapper, delay, unit),
				wrapper);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {		
		RunnableWrapper wrapper = wrapperFor(command);
		return scheduledFuture(
				executor.scheduleAtFixedRate(wrapper, initialDelay, period, unit),
				wrapper);
	}

	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit) {
		RunnableWrapper wrapper = wrapperFor(command);
		return scheduledFuture(
				executor.scheduleWithFixedDelay(wrapper, initialDelay, delay, unit),
				wrapper);
	}

	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		throw new UnsupportedOperationException("Too difficult to track.");
	}

	public <T> List<Future<T>> invokeAll(
			Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		throw new UnsupportedOperationException("Too difficult to track.");
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException("Too difficult to track.");
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		throw new UnsupportedOperationException("Too difficult to track.");
	}

	public boolean isShutdown() {
		return executor.isShutdown();
	}

	public boolean isTerminated() {
		return executor.isTerminated();
	}

	public void shutdown() {
		executor.shutdown();
		synchronized (running) {
			running.notifyAll();
		}
	}

	public List<Runnable> shutdownNow() {
		List<Runnable> outstanding = executor.shutdownNow();
		synchronized (running) {
			running.notifyAll();
		}
		return outstanding;
	}

	public <T> Future<T> submit(Callable<T> task) {
		CallableWrapper<T> wrapper = wrapperFor(task); 
		return future(
				executor.submit(wrapper),
				wrapper);
	}

	public Future<?> submit(Runnable task) {
		RunnableWrapper wrapper = wrapperFor(task);
		return future(
				executor.submit(wrapper),
				wrapper);
	}

	public <T> Future<T> submit(Runnable task, T result) {
		RunnableWrapper wrapper = wrapperFor(task);
		return future(
				executor.submit(wrapper, result),
				wrapper);
	}

	public void execute(Runnable command) {
		executor.execute(wrapperFor(command));
	}

	private RunnableWrapper wrapperFor(Runnable command) {
		
		synchronized (running) {
			RunnableWrapper wrapper = new RunnableWrapper(command);
			running.add(wrapper);
			return wrapper;
		}
	}
	
	private <X> CallableWrapper<X> wrapperFor(Callable<X> callable) {
		
		synchronized (running) {
			CallableWrapper<X> wrapper = new CallableWrapper<X>(callable);
			running.add(wrapper);
			return wrapper;
		}
	}
	
	
	interface Wrapper { }
	
	
	class RunnableWrapper implements Runnable, Wrapper {
		
		private final Runnable runnable;
		
		public RunnableWrapper(Runnable runnable) {
			this.runnable = runnable;
		}

		public void run() {
			try {
				runnable.run();
			}
			finally {
				synchronized (running) {
					running.remove(this);
					running.notifyAll();
				}
			}
		}
	}
	
	class CallableWrapper<V> implements Callable<V>, Wrapper {
	
		private final Callable<V> callable;
		
		public CallableWrapper(Callable<V> callable) {
			this.callable = callable;
		}
		
		public V call() throws Exception {
			try {
				return callable.call();
			}
			finally {
				synchronized (running) {
					running.remove(this);
					running.notifyAll();
				}
			}
		}
	}

	private <T> Future<T> future(Future<T> wrapping, Wrapper wrapper) {
		return new FutureWrapper<T>(wrapping, wrapper);
	}
	
	
	class FutureWrapper<V> implements Future<V> {

		private final Future<V> wrapping;
		private final Wrapper wrapper;
		
		public FutureWrapper(Future<V> wrapping, Wrapper wrapper) {
			this.wrapping = wrapping;
			this.wrapper = wrapper;
		}
		
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (wrapping.cancel(mayInterruptIfRunning)) {
				synchronized (running) {
					running.remove(wrapper);
				}
				return true;
			}
			else {
				return false;
			}
		}
		
		public V get() throws InterruptedException, ExecutionException {
			return wrapping.get();
		}
		
		public V get(long timeout, TimeUnit unit) throws InterruptedException,
				ExecutionException, TimeoutException {
			return wrapping.get(timeout, unit);
		}
		
		public boolean isCancelled() {
			return wrapping.isCancelled();
		}
		
		public boolean isDone() {
			return wrapping.isDone();
		}
	}
	
	private <T> ScheduledFuture<T> scheduledFuture(ScheduledFuture<T> wrapping, Wrapper wrapper) {
		return new ScheduledFutureWrapper<T>(wrapping, wrapper);
	}
	
	
	class ScheduledFutureWrapper<V> extends FutureWrapper<V>
	implements ScheduledFuture<V> {

		private final ScheduledFuture<V> wrapping;
		
		public ScheduledFutureWrapper(ScheduledFuture<V> wrapping, Wrapper wrapper) {
			super(wrapping, wrapper);
			this.wrapping = wrapping;
		}
		
		public int compareTo(Delayed o) {
			return wrapping.compareTo(o);
		}
		
		public long getDelay(TimeUnit unit) {
			return wrapping.getDelay(unit);
		}
	}
}
