package org.oddjob.jobs.structural;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

public class AsyncEnabled {

	private ExecutorService executorService;

	public ExecutorService getExecutorService() {
		return executorService;
	}

	@Inject
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + 
				(executorService == null ? 
						"(No Executor Service)" : executorService);
	}
}
