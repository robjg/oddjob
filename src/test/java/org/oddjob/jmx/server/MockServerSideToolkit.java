package org.oddjob.jmx.server;

import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.remote.Notification;

public class MockServerSideToolkit implements ServerSideToolkit {

	@Override
	public ServerContext getContext() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public Notification createNotification(String type, Object userData) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public void runSynchronized(Runnable runnable) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public void sendNotification(Notification notification) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public RemoteOddjobBean getRemoteBean() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public ServerSession getServerSession() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
