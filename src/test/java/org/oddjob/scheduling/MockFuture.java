package org.oddjob.scheduling;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MockFuture<T> implements Future<T> {

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
}
