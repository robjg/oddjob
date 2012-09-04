/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import java.util.concurrent.Future;

import org.oddjob.scheduling.MockScheduledExecutorService;
import org.oddjob.scheduling.MockScheduledFuture;

/**
 * Dummy NotificationProcessor.
 *
 */
public class DummyNotificationProcessor 
extends MockScheduledExecutorService {

	@Override
	public Future<?> submit(Runnable task) {
		task.run();
		return new MockScheduledFuture<Void>();
	}
	
}
