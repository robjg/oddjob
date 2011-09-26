package org.oddjob;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class MockOddjobExecutors implements OddjobExecutors {

	
	@Override
	public ExecutorService getPoolExecutor() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public ScheduledExecutorService getScheduledExecutor() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
