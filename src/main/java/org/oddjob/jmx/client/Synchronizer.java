package org.oddjob.jmx.client;

import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Synchronises asynchronous notifications with a synchronous class to
 * get initial state.
 * <p>
 * During the synchronisation phase any asynchronous events are queued and
 * processed after synchronisation. Duplicates are detected by the
 * notification number and removed.
 *
 * @author rob
 */
public class Synchronizer<T> implements NotificationListener<T> {

    private final NotificationListener<T> listener;

    private LinkedList<Notification<T>> pending = new LinkedList<>();

    public Synchronizer(NotificationListener<T> listener) {
        this.listener = listener;
    }

    public void handleNotification(Notification<T> notification) {
        synchronized (this) {
            if (pending != null) {
                pending.addLast(notification);
                return;
            }
        }
        listener.handleNotification(notification);
    }

    public final void synchronize(Notification<T> last) {
        if (last == null) {
            synchronize(Collections.emptyList());
        }
        else {
            synchronize(Collections.singletonList(last));
        }
    }

    public final void synchronize(Notification<T>[] last) {
        synchronize(Arrays.asList(last));
    }

    /**
     * Synchronous synchronisation with notifications.
     *
     * @param last The last notifications.
     */
    public final void synchronize(List<Notification<T>> last) {
        long seq = 0;

        for (Notification<T> notification : last) {
            listener.handleNotification(notification);
            seq = notification.getSequence();
        }

        while (true) {
            Notification<T> notification;
            synchronized (this) {
                if (pending.isEmpty()) {
                    pending = null;
                    return;
                }
                notification = pending.removeFirst();
                if (notification.getSequence() < seq) {
                    continue;
                }
            }
            listener.handleNotification(notification);
        }
    }
}
