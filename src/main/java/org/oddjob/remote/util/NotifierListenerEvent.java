package org.oddjob.remote.util;

import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;

import java.util.Objects;

/**
 * The event sent to an {@link NotifierListener}.
 *
 * @param <T> The tye of the notification.
 */
public class NotifierListenerEvent<T> {

    private final NotificationType<T> type;

    private final NotificationListener<T> listener;

    public NotifierListenerEvent(NotificationType<T> type, NotificationListener<T> listener) {
        this.type = Objects.requireNonNull(type);
        this.listener = Objects.requireNonNull(listener);
    }

    public NotificationType<T> getType() {
        return type;
    }

    public NotificationListener<T> getListener() {
        return listener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifierListenerEvent<?> that = (NotifierListenerEvent<?>) o;
        return type.equals(that.type) && listener.equals(that.listener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, listener);
    }
}
