package org.oddjob.scheduling;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.oddjob.OddjobExecutors;

/**
 * Provide Simple Oddjob Services.
 * 
 * @author rob
 *
 */
public class DefaultExecutors implements OddjobExecutors {
	private static final Logger logger = Logger.getLogger(DefaultExecutors.class);
	
	public static final String POOL_SIZE_PROPERTY = "oddjob.executors.default.poolsize";
	
	private int poolSize;
	
	/** The scheduler */
	
	private ExecutorService poolExecutorService;
	
	private ScheduledExecutorService scheduledExecutorService;
		
	public DefaultExecutors() {
		String poolSizeString = System.getProperty(POOL_SIZE_PROPERTY);
		if (poolSizeString == null) {
			poolSize = Runtime.getRuntime().availableProcessors() + 1;
		}
		else {
			poolSize = Integer.parseInt(poolSizeString);
		}
	}
		
	public synchronized void stop() {
		ExecutorService scheduledExecutor;
		ExecutorService poolExecutor;
		synchronized (this) {
			scheduledExecutor = scheduledExecutorService;
			scheduledExecutorService = null;
			poolExecutor = poolExecutorService;
			poolExecutorService = null;
		}
		
		if (poolExecutor != null) {
			logger.info("Shutting down Pool Executor.");
			List<Runnable> running = poolExecutor.shutdownNow();
			logger.info("Shutdown Pool Exector with " + 
					running.size() + " unexecuted jobs.");
		}
		
		if (scheduledExecutor != null) {
			logger.info("Shutting down Scheduled Executor.");
			List<Runnable> running = scheduledExecutor.shutdownNow();
			logger.info("Shutdown Scheduled Exector with " + 
					running.size() + " unexecuted jobs.");
		}
	}

	public ScheduledExecutorService getScheduledExecutor() {
		return startTimerOnFirstRequest();
	}
	
	public ExecutorService getPoolExecutor() {
		return startPoolExecutorOnFirstRequest();
	}

	/**
	 * Provide lazy service starting.
	 * 
	 * @return The OddjobTimerService.
	 */
	private synchronized ScheduledExecutorService startTimerOnFirstRequest() {
		if (scheduledExecutorService == null) {
			logger.info("Starting Scheduled Exector with " + poolSize + " threads.");
			scheduledExecutorService = new ScheduledThreadPoolExecutor(poolSize);
		}
		return scheduledExecutorService;		
	}
	
	private synchronized ExecutorService startPoolExecutorOnFirstRequest() {
		if (poolExecutorService == null) {
			logger.info("Starting Pool Executor.");
			poolExecutorService = Executors.newCachedThreadPool();
		}
		return poolExecutorService;		
	}
	
	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" +
			(poolExecutorService == null ? "not started" : "started") +
			")";
	}
}
