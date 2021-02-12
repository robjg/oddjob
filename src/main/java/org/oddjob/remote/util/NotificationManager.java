package org.oddjob.remote.util;

import org.oddjob.remote.*;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages {@link NotificationListener}s.
 * <p/>
 * Threading guarantees aren't perfect. This needs fixing.
 */
public class NotificationManager implements RemoteNotifier {

    @FunctionalInterface
    public interface Action {
        void perform(long remoteId, NotificationType<?> type) throws RemoteException;
    }

    private final Action subscribeAction;

    private final Action unSubscribeAction;

    private final ConcurrentMap<Long, ByTypeListeners> byRemote =
            new ConcurrentHashMap<>();

    public NotificationManager(Action subscribeAction, Action unSubscribeAction) {
        this.subscribeAction = subscribeAction;
        this.unSubscribeAction = unSubscribeAction;
    }

    private final NotificationListener<?> notificationListener =
            (NotificationListener<Object>) NotificationManager.this::handleNotification;

    public <T> NotificationListener<T> getNotificationListener() {
        return (NotificationListener<T>) notificationListener;
    }

    @Override
    public <T> void addNotificationListener(long remoteId,
                                        NotificationType<T> notificationType,
                                        NotificationListener<T> notificationListener)
            throws RemoteException {

        try {
            byRemote.computeIfAbsent(remoteId, k -> new ByTypeListeners())
                    .addNotificationListener(notificationType, notificationListener,
                            type -> {
                                if (subscribeAction != null) {
                                    subscribeAction.perform(remoteId, type);
                                }
                            });
        }
        catch (RemoteException e) {
            throw new RemoteIdException(remoteId, e);
        }
    }

    @Override
    public <T> void removeNotificationListener(long remoteId,
                                           NotificationType<T> notificationType,
                                           NotificationListener<T> notificationListener) throws RemoteException {

        ByTypeListeners btl = byRemote.get(remoteId);
        if (btl == null) {
            throw new RemoteIdException(remoteId, "No listener " + notificationListener +
                    " of type " + notificationType);
        }

        try {
            // returns true if no more byType
            if (btl.removeNotificationListener(notificationType, notificationListener,
                    (type) ->
                    {
                        if (unSubscribeAction != null) {
                            unSubscribeAction.perform(remoteId, type);
                        }
                    })) {
                byRemote.remove(remoteId);
            };
        }
        catch (RemoteException e) {
            throw new RemoteIdException(remoteId, e);
        }
    }

    /**
     * Dispatch a notification to any listeners.
     *
     * @param notification The notification.
     */
    public void handleNotification(Notification<?> notification) {

        Optional.ofNullable(byRemote.get(notification.getRemoteId()))
                .ifPresent(btl -> btl.dispatch(notification));
    }

    public <T> NotificationListener<T> asListener() {
        return notification -> handleNotification(notification);
    }

    @FunctionalInterface
    private interface WithType<T> {
        void apply(NotificationType<T> type) throws RemoteException;
    }

    static class ByTypeListeners {

        private final ConcurrentMap<NotificationType<?>, Set<NotificationListener<?>>> byType =
                new ConcurrentHashMap<>();

        <T> void addNotificationListener(NotificationType<T> notificationType,
                                     NotificationListener<T> notificationListener,
                                     WithType<T> whenNew) throws RemoteException {

            AtomicBoolean subscribe = new AtomicBoolean();

            Set<NotificationListener<?>> listeners = byType.computeIfAbsent(notificationType, k -> {
                subscribe.set(true);
                return ConcurrentHashMap.newKeySet();
            });

            if (listeners.contains(notificationListener)) {
                throw new RemoteException("Listener " + notificationListener +
                        " already registered for type " + notificationType);
            }
            listeners.add(notificationListener);

            if (subscribe.get()) {
                whenNew.apply(notificationType);
            }
        }

        <T> boolean removeNotificationListener(NotificationType<T> notificationType,
                                        NotificationListener<T> notificationListener,
                                        WithType<T> onEmpty) throws RemoteException {

            Set<NotificationListener<?>> listeners = byType.get(notificationType);

            if (listeners == null) {
                throw new RemoteException("No listener " + notificationListener +
                        " of type " + notificationType);
            }

            if (!listeners.remove(notificationListener)) {
                throw new RemoteException("No listener " + notificationListener +
                        " of type " + notificationType);
            }

            if (listeners.isEmpty()) {
                byType.remove(notificationType);
                onEmpty.apply(notificationType);
            }

            return byType.isEmpty();
        }

        @SuppressWarnings("unchecked")
        <T> void dispatch(Notification<T> notification) {

            Optional.ofNullable(byType.get(notification.getType()))
                    .ifPresent(nls -> nls.forEach(
                            nl -> ((NotificationListener<T>) nl).handleNotification(notification)));
        }
    }

}
