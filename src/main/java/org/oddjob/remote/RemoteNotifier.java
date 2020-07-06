package org.oddjob.remote;

/**
 * Something capable of providing notifications from a remote location.
 */
public interface RemoteNotifier {

    <T> void addNotificationListener(long remoteId,
                                 NotificationType<T> notificationType,
                                 NotificationListener<T> notificationListener)
            throws RemoteException;

    <T> void removeNotificationListener(long remoteId,
                                    NotificationType<T> notificationType,
                                    NotificationListener<T> notificationListener)
            throws RemoteException;


}
