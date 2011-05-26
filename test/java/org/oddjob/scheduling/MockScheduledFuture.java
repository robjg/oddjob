package org.oddjob.scheduling;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MockScheduledFuture<T> implements ScheduledFuture<T> {

	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public T get() throws InterruptedException, ExecutionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public T get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public boolean isCancelled() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public boolean isDone() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public long getDelay(TimeUnit unit) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public int compareTo(Delayed o) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
