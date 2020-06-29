package org.oddjob.jmx.client;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.remote.NotificationListener;


public class MockClientSideToolkit implements ClientSideToolkit {

	@Override
	public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
	throws Throwable {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public ClientSession getClientSession() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public void registerNotificationListener(String eventType,
			NotificationListener notificationListener) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public void removeNotificationListener(String eventType,
			NotificationListener notificationListener) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
