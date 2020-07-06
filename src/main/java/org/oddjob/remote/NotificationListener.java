package org.oddjob.remote;

/**
 * Listens for notifications.
 */
public interface NotificationListener<T> {

    /**
     * Handle a notification.
     *
     * @param notification The notification.
     */
    void handleNotification(Notification<T> notification);
}
