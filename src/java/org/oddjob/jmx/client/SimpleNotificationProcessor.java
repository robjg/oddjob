/*
 * Copyright © 2004, Rob Gordon.
 */
package org.oddjob.jmx.client;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

/**
 * Process notifications from the server. This forces events to be processed
 * in a single thread.
 * <p>
 * Not sure if this is really required but early in development of the JMX
 * client/server it seemed possible to receive child added notifications out
 * of sync, and this seemed to fix it.
 *
 * 
 * @author Rob Gordon.
 */
public class SimpleNotificationProcessor extends Thread
implements NotificationProcessor {
	
	private final Logger logger;
	
	private final LinkedList<Runnable> notifications = new LinkedList<Runnable>();
	private final LinkedList<Delayed> delayed = new LinkedList<Delayed>();

	private final CountDownLatch stopped = new CountDownLatch(1);
	
	private volatile boolean stop;

	/**
	 * Constructor.
	 * 
	 * @param logger
	 */
	public SimpleNotificationProcessor(Logger logger) {
		this.logger = logger;
		logger.debug("Notification processor starting.");
	}
	
	synchronized public void enqueue(Runnable o) {
		if (stop) {
			// late notifications
			return;
		}
		notifications.add(o);
		notify();
	}

	class Delayed {
		final Runnable clientNode;
		final long time;
		final long delay;
		
		Delayed(Runnable clientNode, long delay) {
			this.clientNode = clientNode;
			this.delay = delay;
			this.time = System.currentTimeMillis();
		}
	}
	
	synchronized public void enqueueDelayed(Runnable runnable, long delay) {
		if (stop) {
			// late notifications
			return;
		}		
		logger.debug("Enqued [" + runnable.toString() + 
				"] delayed for " + delay + "ms.");
		delayed.add(new Delayed(runnable, delay));
		notifyAll();
	}
	
	public void run() {
		while (!stop) {
			Runnable runnable = null;
			
			int backlog = 0;
			int checks = 0;
			
			synchronized (this) {
				backlog = notifications.size();
				if (backlog > 0) {
					runnable = notifications.removeFirst();
					--backlog;
				} else {
					long sleep = 0;
					checks = delayed.size();
					if (checks > 0) {
						Delayed pair = delayed.getFirst();
						sleep = pair.time - System.currentTimeMillis() + pair.delay;
						if (sleep < 1) {
							pair = delayed.removeFirst();
							runnable = pair.clientNode;
							--checks;
						}
					}
					if (runnable == null) {
						try {
							wait(sleep);
						}
						catch (InterruptedException e) {
							break;
						}
					}
				}
			}
			if (stop) {
				break;
			}
			logger.debug("Processing, backlog is [" + backlog 
					+ "] immediate, [" + checks + "] delayed.");
			if (runnable != null) {
				try {
					runnable.run();
				} catch (Throwable t) {
					logger.error("Failed processing:", t);
				}
			}
		}
		notifications.clear();
		delayed.clear();
		stopped.countDown();
		logger.debug("Notification processor stopped.");
		
	}
	
	public void stopProcessor() {
		logger.debug("Notification processor stopping.");
		this.stop = true;
		synchronized (this) {
			notifyAll();
		}
		if (Thread.currentThread() != this) {
			try {
				stopped.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	synchronized public int size() {
	    return notifications.size() + delayed.size();
	}
	
}
