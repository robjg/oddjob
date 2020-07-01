package org.oddjob.remote;

/**
 * Provide information on the notifications available to subscribe to.
 */
public interface NotificationInfoProvider {

    /**
     * Get the {@link NotificationInfo} for the given Id. Implementations may either return
     * null or throw an exception if the remote id does not exist.
     *
     * @param remoteId The id
     * @return The notification info or maybe null.
     * @throws RemoteException If something goes wrong.
     */
    NotificationInfo getNotificationInfo(long remoteId)
            throws RemoteException;

}
