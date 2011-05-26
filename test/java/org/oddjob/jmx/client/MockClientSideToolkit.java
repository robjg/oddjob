package org.oddjob.jmx.client;

import javax.management.NotificationListener;

import org.oddjob.jmx.RemoteOperation;


public class MockClientSideToolkit implements ClientSideToolkit {

	public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
	throws Throwable {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public ClientSession getClientSession() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void registerNotificationListener(String eventType,
			NotificationListener notificationListener) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public void removeNotificationListener(String eventType,
			NotificationListener notificationListener) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
