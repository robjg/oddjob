/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.oddjob.OddjobExecutors;

/**
 * A TimerService that doesn't allow it Executor to be shut down. Not used
 * at the moment.
 * 
 * @author Rob Gordon
 * 
 */
public class OddjobTimerService 
implements OddjobExecutors {
	
	private static final Logger logger = Logger.getLogger(OddjobTimerService.class);

	private transient String name;
			
	/** The scheduler */
	private transient ScheduledExecutorService scheduler;
		
	private int poolSize = 5;
		
		
	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	public ExecutorService getPoolExecutor() {
		return new UnstoppableExecutor(scheduler);
	}
	
	public ScheduledExecutorService getScheduledExecutor() {
		return new UnstoppableExecutor(scheduler);
	}
	
	/**
	 * Start the shcheduler.
	 * 
	 * @throws SchedulerException
	 */
	public void start() {
		scheduler = new ScheduledThreadPoolExecutor(poolSize);			
	}
	
	public void stop() {
		logger.debug("Shutting down scheduler.");
				
		// shutdown the scheduler waiting for jobs. Don't close the 
		// persister until the scheduler is closed and jobs have completed.
		scheduler.shutdownNow();
		try {
			while (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
				logger.debug("Waiting for scheduler to terminate...");
			}
		} catch (InterruptedException e) {
			logger.warn("Shutdown wait interrupted.");
		}
		
		scheduler = null;
	}
	
	/**
	 */
	public Date getTimeNow() {
		return new Date();
	}
	
	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		return name;
	}
		
}
