package org.oddjob.jmx.server;

import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.util.NotificationControl;
import org.oddjob.remote.util.NotifierListener;

import java.util.Objects;

/**
 * Main Implementation of an {@link ServerSideToolkit}.
 */
public class ServerSideToolkitImpl implements ServerSideToolkit {

    /**
     * Used to ensure that no fresh notifications are sent during a resync.
     */
    private final Object resyncLock = new Object();

    /**
     * The remoteId of this node
     */
    private final long objectId;

    private final NotificationControl dispatch;

    /**
     * The factory for adding and removing beans.
     */
    private final ServerSession serverSession;

    /**
     * The server context for this OddjobMBean.
     */
    private final ServerContext serverContext;

    private final RemoteOddjobBean remoteBean;

    /**
     * The notification sequenceNumber
     */
    private int sequenceNumber = 0;

    public ServerSideToolkitImpl(long objectId,
                                 NotificationControl dispatch,
                                 ServerSession serverSession,
                                 ServerContext serverContext,
                                 RemoteOddjobBean remoteBean) {
        this.objectId = objectId;
        this.dispatch = Objects.requireNonNull(dispatch);
        this.serverSession = Objects.requireNonNull(serverSession);
        this.serverContext = Objects.requireNonNull(serverContext);
        this.remoteBean = remoteBean;
    }

    public static ServerSideToolkit create(long objectId,
                                           NotificationControl dispatch,
                                           ServerSession serverSession,
                                           ServerContext serverContext,
                                           RemoteOddjobBean remoteBean) {

        return new ServerSideToolkitImpl(objectId, dispatch, serverSession, serverContext,
                remoteBean);
    }

    @Override
    public void sendNotification(org.oddjob.remote.Notification<?> notification) {
        dispatch.sendNotification(notification);
    }

    @Override
    public <T> void setNotifierListener(NotificationType<T> type, NotifierListener<T> notifierListener) {
        dispatch.setNotifierListener(type, notifierListener);
    }

    @Override
    public <T> org.oddjob.remote.Notification<T> createNotification(NotificationType<T> type, T userData) {
        synchronized (resyncLock) {
            return new org.oddjob.remote.Notification<>(objectId, type, sequenceNumber++, userData);
        }
    }


    /**
     * Used by handlers to execute functionality while
     * holding the resync lock.
     *
     * @param runnable The functionality to run.
     */
    public void runSynchronized(Runnable runnable) {
        synchronized (resyncLock) {
            runnable.run();
        }
    }

    /**
     * Gives handlers access to the server context.
     *
     * @return The server context for this MBean.
     */
    public ServerContext getContext() {
        return serverContext;
    }

    public RemoteOddjobBean getRemoteBean() {
        return remoteBean;
    }

    public ServerSession getServerSession() {
        return serverSession;
    }


}
