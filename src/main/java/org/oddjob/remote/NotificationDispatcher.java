package org.oddjob.remote;

/**
 * Something that is able to dispatch notifications.
 */
public interface NotificationDispatcher {

    /**
     * Send a notification.
     *
     * @param notification The notification.
     */
    void sendNotification(Notification<?> notification);
}
