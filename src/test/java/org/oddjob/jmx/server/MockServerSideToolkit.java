package org.oddjob.jmx.server;

import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.util.NotifierListener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockServerSideToolkit implements ServerSideToolkit {


	@Override
	public long getRemoteId() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public ServerContext getContext() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public <T> Notification<T> createNotification(NotificationType<T> type, T userData) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public <T> void setNotifierListener(NotificationType<T> type, NotifierListener<T> notifierListener) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public void runSynchronized(Runnable runnable) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public void sendNotification(Notification<?> notification) {
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

	public static ServerSideToolkit mockToolkit(long remoteId) {

		ServerSideToolkit serverSideToolkit = mock(ServerSideToolkit.class);
		when(serverSideToolkit.getRemoteId()).thenReturn(remoteId);

		return serverSideToolkit;
	}
}
