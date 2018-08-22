package org.oddjob.scheduling;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MockScheduledExecutorService extends MockExecutorService 
implements ScheduledExecutorService {

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay,
			TimeUnit unit) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
			TimeUnit unit) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
