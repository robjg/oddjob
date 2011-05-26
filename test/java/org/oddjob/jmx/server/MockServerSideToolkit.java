package org.oddjob.jmx.server;

import javax.management.Notification;

import org.oddjob.jmx.RemoteOddjobBean;

public class MockServerSideToolkit implements ServerSideToolkit {

	public ServerContext getContext() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Notification createNotification(String type) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public void runSynchronized(Runnable runnable) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public void sendNotification(Notification notification) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public RemoteOddjobBean getRemoteBean() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public ServerSession getServerSession() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
