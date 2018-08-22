package org.oddjob.scheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.oddjob.OddjobExecutors;

public class TrackingServices implements OddjobExecutors {

	private final TrackingExecutor executor;
	
	public TrackingServices(int poolSize) {
		
		executor = new TrackingExecutor(
				new ScheduledThreadPoolExecutor(poolSize));
		
	}
	
	public ExecutorService getPoolExecutor() {
		return executor;
	}
	
	public ScheduledExecutorService getScheduledExecutor() {
		return executor;
	}
	
	public void stop() throws InterruptedException {
		executor.waitForNothingOutstanding();
		executor.shutdown();
	}
}
