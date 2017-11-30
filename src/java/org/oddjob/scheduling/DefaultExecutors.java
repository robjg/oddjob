package org.oddjob.scheduling;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.OddjobExecutors;

/**
 * Provide Simple Oddjob Services.
 * 
 * @author rob
 *
 */
public class DefaultExecutors implements OddjobExecutors {
	private static final Logger logger = LoggerFactory.getLogger(DefaultExecutors.class);
	
	public static final String POOL_SIZE_PROPERTY = "oddjob.executors.default.poolsize";
	
	/** The pool size for the {@link ScheduledExecutorService}. */
	private int poolSize;
	
	/** share Executor and Scheduled services. */
	private boolean shareServices;

	/** The standard executor service. */
	private ExecutorService poolExecutorService;
	
	/** The scheduler */
	private ScheduledExecutorService scheduledExecutorService;
		
	/**
	 * Create a new instance. The scheduler is initialised with a 
	 * fixed pool size that is either from the system property
	 * or based on the number of available processors discovered at
	 * runtime. The pool size can also be set by the property.
	 */
	public DefaultExecutors() {
		String poolSizeString = System.getProperty(POOL_SIZE_PROPERTY);
		if (poolSizeString == null) {
			poolSize = Runtime.getRuntime().availableProcessors() + 1;
		}
		else {
			poolSize = Integer.parseInt(poolSizeString);
		}
	}
		
	/**
	 * Stop the services.
	 */
	public void stop() {
		
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

	/*
	 * (non-Javadoc)
	 * @see org.oddjob.OddjobExecutors#getScheduledExecutor()
	 */
	public ScheduledExecutorService getScheduledExecutor() {
		return startTimerOnFirstRequest();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.OddjobExecutors#getPoolExecutor()
	 */
	public ExecutorService getPoolExecutor() {
		if (shareServices) {
			return getScheduledExecutor();
		}
		else {
			return startPoolExecutorOnFirstRequest();
		}
	}

	/**
	 * Provide lazy service starting.
	 * 
	 * @return The scheduled executor service.
	 */
	private synchronized ScheduledExecutorService startTimerOnFirstRequest() {
		
		if (scheduledExecutorService == null) {
			
			logger.info("Starting Scheduled Exector with " + poolSize + " threads.");
			
			scheduledExecutorService = new OddjobScheduledExecutorService(
					new ScheduledThreadPoolExecutor(poolSize));
		}
		
		return scheduledExecutorService;		
	}
	
	/**
	 * Provide lazy starting of a pool executor.
	 * 
	 * @return The executor service.
	 */
	private synchronized ExecutorService startPoolExecutorOnFirstRequest() {

		if (poolExecutorService == null) {

			logger.info("Starting Pool Executor.");
			
			poolExecutorService = new OddjobExecutorService(
					new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>()));
		}
		
		return poolExecutorService;		
	}
	
	/**
	 * Get the pool size for the scheduled executor.
	 * 
	 * @return
	 */
	public int getPoolSize() {
		return poolSize;
	}

	/**
	 * Set the pool size for the scheduled executor.
	 * 
	 * @param poolSize The pool size.
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public boolean isShareServices() {
		return shareServices;
	}

	/**
	 * Share the same service between the executor service and the scheduled
	 * executor service.
	 * 
	 * @param shareServices
	 */
	public void setShareServices(boolean shareServices) {
		this.shareServices = shareServices;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" +
			((poolExecutorService == null && 
				scheduledExecutorService == null) ? "not started" : "started") +
			")";
	}
}
