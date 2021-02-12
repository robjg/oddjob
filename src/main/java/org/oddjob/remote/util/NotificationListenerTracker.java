package org.oddjob.remote.util;


import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.RemoteException;
import org.oddjob.remote.RemoteNotifier;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks listeners being added to an {@link org.oddjob.remote.RemoteNotifier} and allows listeners to be removed
 * all at once, i.e. when a node is destroyed.
 *
 * @param <G> The type of the grouping, i.e. a session id.
 *
 */
public class NotificationListenerTracker<G> {

    private final RemoteNotifier remoteNotifier;

    private final ByGroup<G> byGroup = new ByGroup<>();

    public NotificationListenerTracker(RemoteNotifier remoteNotifier) {
        this.remoteNotifier = remoteNotifier;
    }

    public <T> void addNotificationListener(G group, long remoteId, NotificationType<T> type,
                                            NotificationListener<T> listener) throws RemoteException {
        byGroup.put(group, remoteId, type, listener);
        try {
            remoteNotifier.addNotificationListener(remoteId, type, listener);
        }
        catch (RemoteException | RuntimeException e) {
            byGroup.remove(group, remoteId, type);
        }
    }

    public <T> void removeNotificationListener(G group, long remoteId,
                                               NotificationType<T> type) throws RemoteException {

        NotificationListener<T> listener = byGroup.remove(group, remoteId, type);

        remoteNotifier.removeNotificationListener(remoteId, type, listener);
    }

    public boolean removeAll(G group) throws RemoteException {

        return byGroup.removeAll(group, remoteNotifier);
    }

    static class ByGroup<G> {

        private final ConcurrentMap<G, ByRemoteId> byGroupMap = new ConcurrentHashMap<>();

        <T> void put(G group, long remoteId, NotificationType<T> type, NotificationListener<T> listener) {
            byGroupMap.computeIfAbsent(group, (k) -> new ByRemoteId(() -> byGroupMap.remove(group)))
                    .put(remoteId, type, listener);
        }

        <T> NotificationListener<T> remove(G group, long remoteId, NotificationType<T> type) {
            return Optional.ofNullable(byGroupMap.get(group))
                    .orElseThrow(() -> new IllegalArgumentException("Listener not registered, group=" +
                            group + "remoteId=" + remoteId + ", type=" + type))
                    .remove(remoteId, type);
        }

        boolean removeAll(G group, RemoteNotifier notifier) throws RemoteException {

            ByRemoteId byRemoteId = byGroupMap.remove(group);

            if (byRemoteId == null) {
                return false;
            }
            else {
                byRemoteId.removeAll(notifier);
                return true;
            }
        }
    }

    static class ByRemoteId {

        private final ConcurrentMap<Long, ByType> byTypeMap;

        ByRemoteId(Runnable emptyAction) {
            byTypeMap = new OnEmptyMap<>(emptyAction);
        }

        <T> void put(long remoteId, NotificationType<T> type, NotificationListener<T> listener) {
            byTypeMap.computeIfAbsent(remoteId,
                    (k) -> new ByType(() -> byTypeMap.remove(remoteId)))
                    .put(type, listener);
        }

        <T> NotificationListener<T> remove(long remoteId, NotificationType<T> type) {
            return Optional.ofNullable(byTypeMap.get(remoteId))
                    .orElseThrow(() -> new IllegalArgumentException("Listener not registered, remoteId=" +
                            remoteId + ", type=" + type))
                    .remove(type);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        void removeAll(RemoteNotifier remoteNotifier) throws RemoteException {

            AtomicReference<RemoteException> eRef = new AtomicReference<>();

            byTypeMap.forEach((remoteId, byType) ->
                    byType.listenerMap.forEach( (NotificationType type, NotificationListener listener) -> {
                                try {
                                    remoteNotifier.removeNotificationListener(remoteId, type, listener);
                                } catch (RemoteException e) {
                                    eRef.set(e);
                                }
                            }));

            if (eRef.get() != null) {
                throw eRef.get();
            }
        }
    }

    static class ByType {

        private final ConcurrentMap<NotificationType<?>, NotificationListener<?>> listenerMap;

        ByType(Runnable emptyAction) {
            listenerMap = new OnEmptyMap<>(emptyAction);
        }

        <T> void put(NotificationType<T> type, NotificationListener<T> listener) {
            NotificationListener<?> previous = listenerMap.putIfAbsent(type, listener);
            if (previous != null) {
                throw new IllegalArgumentException("Listener already registered, type=" + type +
                        ", listener=" + listener);
            }
        }

        @SuppressWarnings("unchecked")
        <T> NotificationListener<T> remove(NotificationType<T> type) {
            return Optional.ofNullable((NotificationListener<T>) listenerMap.remove(type))
                    .orElseThrow(() -> new IllegalArgumentException("Listener not registered, type=" + type));
        }
    }

    static class OnEmptyMap<K, V> extends ConcurrentHashMap<K, V> {

        private final Runnable emptyAction;

        OnEmptyMap(Runnable emptyAction) {
            this.emptyAction = emptyAction;
        }

        @Override
        public V remove(Object key) {
            V previous =  super.remove(key);
            if (this.isEmpty()) {
                emptyAction.run();
            }
            return previous;
        }
    }
}
