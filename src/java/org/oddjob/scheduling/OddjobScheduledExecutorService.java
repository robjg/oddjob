package org.oddjob.scheduling;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A wrapper for {@link ScheduledThreadPoolExecutor} that provides an 
 * {@link #toString()} implementation.
 * 
 * @author rob
 *
 */
public class OddjobScheduledExecutorService extends OddjobExecutorService 
implements ScheduledExecutorService {
	
	private final ScheduledThreadPoolExecutor delegate;
	
	public OddjobScheduledExecutorService(
			ScheduledThreadPoolExecutor delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay,
			TimeUnit unit) {
		return delegate.schedule(command, delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
			TimeUnit unit) {
		return delegate.schedule(callable, delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {
		return delegate.scheduleAtFixedRate(
				command, initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit) {
		return delegate.scheduleWithFixedDelay(
				command, initialDelay, delay, unit);
	}
	
	@Override
	public String toString() {
		return super.toString() + ", scheduled=" + 
				delegate.getQueue().size();
	}

}
