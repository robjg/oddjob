package org.oddjob.remote.util;

import org.oddjob.remote.NotificationType;

/**
 * Something able to notify when a {@link org.oddjob.remote.NotificationListener} is added or removed.
 * At the moment we assume that only one Handler will be interested, so we support only one {@link NotifierListener}.
 * This might change.
 */
public interface NotificationNotifier {

    /**
     * Sets an {@link NotifierListener}.
     *
     * @param type The notification type.
     * @param notifierListener The listener.
     * @param <T> The type of the notification and listener.
     */
    <T> void setNotifierListener(NotificationType<T> type, NotifierListener<T> notifierListener);

}
