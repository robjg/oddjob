package org.oddjob.remote.util;

/**
 * Something to be notified when an {@link org.oddjob.remote.NotificationListener} is added
 * to an {@link org.oddjob.remote.RemoteNotifier}.
 * <p>
 *     This allows {@link org.oddjob.jmx.server.ServerInterfaceHandler}s to resend the last notification
 *     rather than relying on a resync method.
 * </p>
 * @param <T> The type of the notification.
 */
public interface NotifierListener<T> {

    void notificationListenerAdded(NotifierListenerEvent<T> event);

    void notificationListenerRemoved(NotifierListenerEvent<T> event);

}
