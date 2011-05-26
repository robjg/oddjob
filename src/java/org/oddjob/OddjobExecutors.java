package org.oddjob;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An Abstraction for providing Oddjob with java {@link ExecutorService} 
 * implementations. Oddjob requires these for running things in parallel, 
 * asynchronously triggering jobs, and shceduling jobs.
 * 
 * @author rob
 *
 */
public interface OddjobExecutors {

	/**
	 * Provide a {@link ScheduledExecutorService}.
	 * 
	 * @return A ScheduledExecutorService. Never null.
	 */
	public ScheduledExecutorService getScheduledExecutor();
	
	/**
	 * Provide a {@link ExecutorService}. Implementations are free to
	 * use {@link #getScheduledExecutor()} for this, or provide 
	 * something different.
	 * 
	 * @return An ExecutorService. Never null.
	 */
	public ExecutorService getPoolExecutor();
	
}
