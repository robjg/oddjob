/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import org.apache.log4j.Logger;

/**
 * Dummy NotificationProcessor.
 *
 */
public class DummyNotificationProcessor extends SimpleNotificationProcessor {

	public DummyNotificationProcessor(Logger logger) {
		super(logger);
	}
	
	public synchronized void enqueue(Runnable o) {
		o.run();
	}
}
