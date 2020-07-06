package org.oddjob.jmx.client;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;


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
    public <T> void registerNotificationListener(NotificationType<T> eventType,
                                                 NotificationListener<T> notificationListener) {
        throw new RuntimeException("Unexpected from " + getClass());
    }

    @Override
    public <T> void removeNotificationListener(NotificationType<T> eventType,
                                               NotificationListener<T> notificationListener) {
        throw new RuntimeException("Unexpected from " + getClass());
    }
}
