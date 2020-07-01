package org.oddjob.remote;

/**
 * Something capable of providing notifications from a remote location.
 */
public interface RemoteNotifier extends NotificationInfoProvider {

    void addNotificationListener(long remoteId,
                                 String notificationType,
                                 NotificationListener notificationListener)
            throws RemoteException;

    void removeNotificationListener(long remoteId,
                                    String notificationType,
                                    NotificationListener notificationListener)
            throws RemoteException;


}
