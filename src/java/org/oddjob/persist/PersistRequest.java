/*
 * Copyright © 2004, Rob Gordon.
 */
package org.oddjob.persist;

import java.io.File;

/**
 *
 * @author Rob Gordon.
 */
public class PersistRequest {

	private final Object toPersist;
	private final String id;
	private final File directory;
	private boolean persisted = false;
	private final Object waitOn = new Object();

	public PersistRequest(Object toPersist, String id, 
	        File directory) {
		this.toPersist = toPersist;
		this.id = id;
		this.directory = directory;
	}

	public Object getToPersist() {
		return toPersist;
	}
	
	public String getId() {
		return id;
	}
	
	public File getDirectory() {
	    return directory;
	}
	
	public void persisted() {
		synchronized (waitOn) {
			persisted = true;
			waitOn.notifyAll();
		}
	}

	public void waitPerist() {
		synchronized (waitOn) {
			while (!persisted) {
				try {
					waitOn.wait();
				}
				catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}
}
