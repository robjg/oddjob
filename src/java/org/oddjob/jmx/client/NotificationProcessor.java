/*
 * Copyright © 2004, Rob Gordon.
 */
package org.oddjob.jmx.client;


/**
 * Process notifications from the server. Additionally this will asynchronously
 * perform a resync check. This is because it appears possible to 
 * occasionally miss the first few notifications when creating a new node.
 * 
 * @author Rob Gordon.
 */
public interface NotificationProcessor {

	public void enqueue(Runnable o);
	
	public void enqueueDelayed(Runnable runnable, long delay);
	
	public void run();
	
	public int size();
	
}
