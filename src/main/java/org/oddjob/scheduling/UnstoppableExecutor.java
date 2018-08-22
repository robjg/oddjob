package org.oddjob.scheduling;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class UnstoppableExecutor extends ScheduledExecutorServiceAdaptor {

	public UnstoppableExecutor(ScheduledExecutorService delegate) {
		super(delegate);
	}
	
	@Override
	public void shutdown() {
		throw new UnsupportedOperationException("Only the owner can shut down!");
	}
	
	@Override
	public List<Runnable> shutdownNow() {
		throw new UnsupportedOperationException("Only the owner can shut down!");
	}
	
	
}
