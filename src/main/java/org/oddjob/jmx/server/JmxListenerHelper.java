package org.oddjob.jmx.server;

import org.oddjob.jmx.general.RemoteBridge;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.util.NotifierListener;
import org.oddjob.remote.util.NotifierListenerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manage JMX listeners on behalf of an {@link OddjobMBean}. When a listener is added or removed any set
 * {@link NotifierListener} of that type is notified.
 */
public class JmxListenerHelper implements NotificationEmitter, org.oddjob.remote.util.NotificationControl {

    private static final Logger logger = LoggerFactory.getLogger(JmxListenerHelper.class);

    private final ObjectName objectName;

    private final ConcurrentMap<NotificationType<?>, List<ListenerAndHandback>> listeners = new ConcurrentHashMap<>();

    private final ConcurrentMap<NotificationType<?>, NotifierListener<?>> notifierListeners = new ConcurrentHashMap<>();

    public JmxListenerHelper(ObjectName objectName) {
        this.objectName = objectName;
    }

    static class ListenerAndHandback {
        private final NotificationListener notificationListener;
        private final Object handback;


        ListenerAndHandback(NotificationListener notificationListener, Object handback) {
            this.notificationListener = notificationListener;
            this.handback = handback;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ListenerAndHandback that = (ListenerAndHandback) o;
            return notificationListener.equals(that.notificationListener);
        }

        @Override
        public int hashCode() {
            return Objects.hash(notificationListener);
        }
    }

    /**
     * Set the notification types supported. This should be done in the constructor but there is a
     * chicken and egg situation with handlers needing to add {@link NotifierListener}s during construction
     * before all types are known.
     *
     * @param notificationTypes The notification types supported.
     */
    public void setNotificationTypes(Set<NotificationType<?>> notificationTypes) {
        notificationTypes.forEach(t -> this.listeners.put(t, new CopyOnWriteArrayList<>()));
    }

    @Override
    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException {

        // Why do we support multiple types here?
        List<NotificationType<?>> addedTo = new LinkedList<>();

        listeners.forEach( (t, ls) -> {
            if (RemoteBridge.isFilterForType(filter, t)) {
                ls.add(new ListenerAndHandback(listener, handback));
                fireListenerAddedInferType(t, listener);
                addedTo.add(t);
            }
        });

        if (addedTo.isEmpty()) {
            logger.warn("{} Listener ignored as not enabled for any types: {}, filter={}", objectName, listener, filter);
        }
        else {
            logger.debug("{} Listener {} added for types {}, filter={}", objectName, listener, addedTo, filter);
        }
    }

    @Override
    public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException {

        this.removeNotificationListener(listener);
    }

    @Override
    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {

        List<NotificationType<?>> removedFrom = new LinkedList<>();

        ListenerAndHandback listenerAndHandback = new ListenerAndHandback(listener, null);
        listeners.forEach((t, ll) -> {
            if (ll.remove(listenerAndHandback)) {
                fireListenerRemovedInferType(t, listener);
                removedFrom.add(t);
            }
        });

        if (removedFrom.isEmpty()) {
            logger.warn("{} Listener not removed from any types: {}", objectName, listener);
        }
        else {
            logger.debug("{} Listener {} removed for types {}", objectName, listener, removedFrom);
        }
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {

        return listeners.keySet().stream()
                .map(RemoteBridge::toMBeanNotification)
                .toArray(MBeanNotificationInfo[]::new);
    }

    @SuppressWarnings("unchecked")
    <T> void fireListenerAddedInferType(NotificationType<T> notificationType,
                                        NotificationListener listener) {
        Optional.ofNullable(notifierListeners.get(notificationType))
                .ifPresent(l -> ((NotifierListener<T>) l).notificationListenerAdded(
                        new NotifierListenerEvent<>(notificationType,
                                RemoteBridge.toRemoteListener(objectName, listener))));
    }

    @SuppressWarnings("unchecked")
    <T> void fireListenerRemovedInferType(NotificationType<T> notificationType,
                                          NotificationListener listener) {
        Optional.ofNullable(notifierListeners.get(notificationType))
                .ifPresent(l -> ((NotifierListener<T>) l).notificationListenerRemoved(
                        new NotifierListenerEvent<>(notificationType,
                                RemoteBridge.toRemoteListener(objectName, listener))));
    }

    @Override
    public <T> void setNotifierListener(NotificationType<T> type, NotifierListener<T> notifierListener) {
        if (notifierListeners.containsKey(type)) {
            throw new IllegalArgumentException("NotifierListener already set");
        }

        // Would be good to check type is enabled but this call might happen before types are known,
        // so we can't do that check. TODO is this still the case?

        notifierListeners.put(type, notifierListener);
    }

    /**
     * Send a notification.
     *
     * @param notification The notification.
     */
    @Override
    public void sendNotification(org.oddjob.remote.Notification<?> notification) {

        List<ListenerAndHandback> ll = listeners.get(notification.getType());
        if (ll == null) {
            logger.trace("No listeners for {}, not sending anything.", notification);
        }
        else {
            logger.debug("Sending {} to {} listeners", notification, ll.size());
            Notification jmxNotification = RemoteBridge.toJmxNotification(objectName, notification);
            ll.forEach(listenerAndHandback -> {
                try {
                    listenerAndHandback.notificationListener
                            .handleNotification(jmxNotification, listenerAndHandback.handback);
                } catch (Throwable t) {
                    logger.error("Notification Listener " + listenerAndHandback.notificationListener +
                            ", threw exception.", t);
                }
            });
        }
    }
}