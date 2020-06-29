package org.oddjob.remote;

/**
 * Listens for notifications.
 */
public interface NotificationListener {

    /**
     * Handle a notification.
     *
     * @param notification The notification.
     */
    void handleNotification(Notification notification);
}
