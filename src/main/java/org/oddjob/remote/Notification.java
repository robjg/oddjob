package org.oddjob.remote;

import java.io.Serializable;
import java.util.Objects;

/**
 * A Notification.
 */
public class Notification<T> implements Serializable {

    private static final long serialVersionUID = 2020062900L;

    private final long remoteId;

    private final NotificationType<T> type;

    private final long sequence;

    private final T data;

    public Notification(long remoteId, NotificationType<T> type, long sequence) {
        this(remoteId, type, sequence, null);
    }

    /**
     * Create a notification.
     *
     * @param remoteId The remote id of the thing emitting the notification.
     * @param type The type of notification.
     * @param sequence The sequence number.
     * @param data Any user data. May be null.
     */
    public Notification(long remoteId, NotificationType<T> type, long sequence, T data) {
        this.remoteId = remoteId;
        this.type = Objects.requireNonNull(type);
        this.sequence = sequence;
        this.data = data;
    }

    public long getRemoteId() {
        return remoteId;
    }

    public NotificationType<T> getType() {
        return type;
    }

    public long getSequence() {
        return sequence;
    }

    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return remoteId == that.remoteId &&
                sequence == that.sequence &&
                type.equals(that.type) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remoteId, type, sequence, data);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "remoteId=" + remoteId +
                ", type='" + type + '\'' +
                ", sequence=" + sequence +
                ", data=" + data +
                '}';
    }
}
