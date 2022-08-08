package org.oddjob.jmx.general;

import org.oddjob.arooa.utils.ClassUtils;
import org.oddjob.jmx.server.OddjobMBeanFactory;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Bridge between the Oddjob Generic Remote API and JMX remoting.
 */
public class RemoteBridge implements RemoteConnection {

    private static final Logger logger = LoggerFactory.getLogger(RemoteBridge.class);

    private final MBeanServerConnection mbsc;

    /** Key for Listeners. So the JMX Listener can be removed. The JMX Listener must be the exact same
     * one. Object equality with Equals is not supported when removing listeners in the
     * {@code MBeanServerConnection} implementation. */
    static class Key<T> {
        private final NotificationType<T> notificationType;
        private final org.oddjob.remote.NotificationListener<T> listener;

        Key(NotificationType<T> notificationType, NotificationListener<T> listener) {
            this.notificationType = Objects.requireNonNull(notificationType);
            this.listener = Objects.requireNonNull(listener);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key<?> key = (Key<?>) o;
            return notificationType.equals(key.notificationType) && listener.equals(key.listener);
        }

        @Override
        public int hashCode() {
            return Objects.hash(notificationType, listener);
        }

        @Override
        public String toString() {
            return "Key{" +
                    "notificationType=" + notificationType +
                    ", listener=" + listener +
                    '}';
        }
    }
    private final ConcurrentMap<Long, ConcurrentMap<Key<?>, javax.management.NotificationListener>> jmxListeners =
            new ConcurrentHashMap<>();

    public RemoteBridge(MBeanServerConnection mbsc) {
        this.mbsc = Objects.requireNonNull(mbsc);
    }

    @Override
    public <T> void addNotificationListener(long remoteId,
                                            NotificationType<T> notificationType,
                                            NotificationListener<T> notificationListener)
            throws RemoteException {

        javax.management.NotificationListener jmxListener =
                toJmxListener(remoteId, notificationListener, notificationType.getDataType());

        NotificationFilter filter = createTypeFilterFor(notificationType);

        ObjectName objectName = OddjobMBeanFactory.objectName(remoteId);
        try {
            mbsc.addNotificationListener(objectName,
                    jmxListener,
                    filter,
                    null);
        } catch (InstanceNotFoundException | IOException e) {
            throw new RemoteIdException(remoteId, e);
        }

        Key<T> key = new Key<>(notificationType, notificationListener);

        javax.management.NotificationListener previous = jmxListeners.computeIfAbsent(
                remoteId, k -> new ConcurrentHashMap<>())
                .put(key, jmxListener);

        if (previous == null) {
            logger.trace("Added Listener for remoteId, {}, {}", remoteId, key);
        }
        else {
            try {
                mbsc.removeNotificationListener(objectName, previous);
            } catch (InstanceNotFoundException | ListenerNotFoundException | IOException e) {
                logger.error("Failed removing overridden listener {}", previous, e);
            }
            throw new RemoteIdException(remoteId, "Listener already existed for remote id [" +
                    remoteId + "], type [" + notificationType + "], listener [" + notificationListener + "]");
        }
    }

    @Override
    public <T> void removeNotificationListener(long remoteId,
                                               NotificationType<T> notificationType,
                                               NotificationListener<T> notificationListener)
            throws RemoteException {

        Key<T> key = new Key<>(notificationType, notificationListener);

        Map<Key<?>, javax.management.NotificationListener> listenerMap = jmxListeners.get(remoteId);

        if (listenerMap == null) {
            throw new RemoteIdException(remoteId, "No Listener for " + key + ", remoteId " + remoteId);
        }

        javax.management.NotificationListener jmxListener = listenerMap.remove(key);

        if (jmxListener ==  null) {
            throw new RemoteIdException(remoteId, "No Listener for " + key + ", remoteId " + remoteId);
        }

        if (listenerMap.isEmpty()) {
            jmxListeners.remove(remoteId);
        }

        removeListenerInferType(remoteId, jmxListener);
    }

    @Override
    public <T> T invoke(long remoteId, OperationType<T> operationType, Object... args) throws RemoteException {

        String[] signature = ClassUtils.classesToStrings(operationType.getSignature());

        Object result;
        try {
            logger.trace("Invoking {} on {}", operationType, remoteId);

            result = mbsc.invoke(OddjobMBeanFactory.objectName(remoteId),
                    operationType.getName(),
                    args,
                    signature);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | IOException e) {
            throw new RemoteIdException(remoteId, e);
        }

        return ClassUtils.cast(operationType.getReturnType(), result);
    }

    @Override
    public void destroy(long remoteId) throws RemoteException {

        Map<Key<?>, javax.management.NotificationListener> listenerMap = jmxListeners.remove(remoteId);

        if (listenerMap == null) {
            logger.trace("Destroy {}, all cleanup already.", remoteId);
        }
        else {
            logger.warn("Destroy {}, cleanup required for {}. This needs fixing!", remoteId, listenerMap);
            for (javax.management.NotificationListener jmxListener : listenerMap.values()) {
                removeListenerInferType(remoteId, jmxListener);
            }
        }
    }

    <T> void  removeListenerInferType(long remoteId, javax.management.NotificationListener jmxListener) throws RemoteException {
        ObjectName objectName = OddjobMBeanFactory.objectName(remoteId);
        try {
            // A client may be trying to remove notification listeners from a destroyed component.
            if (mbsc.isRegistered(objectName)) {
                mbsc.removeNotificationListener(objectName, jmxListener);
                logger.trace("Removed JMX Listener {}", jmxListener);
            }
            else {
                logger.debug("Not removing JMX Listener {} as MBean {} has already been removed",
                        jmxListener, objectName);
            }
        } catch (InstanceNotFoundException | ListenerNotFoundException | IOException e) {
            throw new RemoteIdException(remoteId, e);
        }
    }

    /**
     * Create a JMX filter for Notification Type.
     *
     * @param type Notification Type.
     *
     * @return The Filter.
     *
     * @see #isFilterForType(NotificationFilter, NotificationType)
     */
    public static NotificationFilter createTypeFilterFor(NotificationType<?> type) {
        return new FilterAdaptor(type);
    }

    /**
     * Convert a JMX Notification into a Remote Notification.
     *
     * @param remoteId The Remote id this is a Notification for.
     * @param dataType The type of the Notification.
     * @param notification The Notification.
     *
     * @param <T> The type of the Notification.
     *
     * @return A Remote Notification.
     */
    @SuppressWarnings("unchecked")
    public static <T> Notification<T> toRemoteNotification(long remoteId,
                                                           Class<T> dataType,
                                                           javax.management.Notification notification) {
        return new Notification<>(remoteId,
                new NotificationType<>(notification.getType(), dataType),
                notification.getSequenceNumber(),
                (T) notification.getUserData());
    }

    /**
     * Convert a Remote Notification into a JMX Notification.
     *
     * @param objectName The Object Name this is a Notification for.
     * @param notification The Remote Notification.
     *
     * @return An JMX Notification.
     */
    public static javax.management.Notification toJmxNotification(ObjectName objectName,
                                                                  Notification<?> notification) {
        javax.management.Notification jmxNotification =
                new javax.management.Notification(notification.getType().getName(),
                        objectName,
                        notification.getSequence());
        jmxNotification.setUserData(notification.getData());
        return jmxNotification;
    }

    /**
     * Convert an JMX Listener to a Remote Listener. This will take a shortcut if the JMX Listener is
     * an {@link JmxListenerAdaptor}.
     *
     * @param objectName The object name the JMX Listener is for.
     * @param jmxListener The JMX Listener
     * @param <T> The type of the Notification.
     *
     * @return A Remote Listener.
     */
    public static <T> org.oddjob.remote.NotificationListener<T> toRemoteListener(
            ObjectName objectName, javax.management.NotificationListener jmxListener) {
        if (jmxListener instanceof JmxListenerAdaptor) {
            //noinspection rawtypes,unchecked
            return (org.oddjob.remote.NotificationListener<T>) ((JmxListenerAdaptor) jmxListener).listener;
        } else {
            return new RemoteListenerAdaptor<>(objectName, jmxListener);
        }
    }

    /**
     * Convert a Remote listener to an JMX Listener. This will take a shortcut if the Remote Listener
     * is an {@link RemoteListenerAdaptor}.
     *
     * @param remoteId The id this remote listener is for.
     * @param remoteListener The Remote Listener.
     * @param dataType The class name of the Notification type.
     *
     * @param <T> The type The Notification Type.
     *
     * @return An JMX Listener.
     */
    public static <T> javax.management.NotificationListener toJmxListener(
            long remoteId, org.oddjob.remote.NotificationListener<T> remoteListener, Class<T> dataType) {
        if (remoteListener instanceof  RemoteListenerAdaptor) {
            return ((RemoteListenerAdaptor<T>) remoteListener).jmxListener;
        }
        else {
            return new JmxListenerAdaptor<>(remoteId, remoteListener, dataType);
        }
    }

    /**
     * Is a Filter for a given type. Provides a shortcut if the Filter is an {@link FilterAdaptor}.
     *
     * @param filter The filter.
     * @param notificationType The Notification Type.
     *
     * @return True if it is, false otherwise.
     */
    public static boolean isFilterForType(NotificationFilter filter, NotificationType<?> notificationType) {
        if (filter == null) {
            return true;
        }
        if (filter instanceof FilterAdaptor) {
            return ((FilterAdaptor) filter).notificationType.equals(notificationType);
        }
        else {
            javax.management.Notification dummy = new javax.management.Notification(
                    notificationType.getName(), new Object(), 0);
            return filter.isNotificationEnabled(dummy);
        }
    }

    /**
     * Convert an {@link NotificationType} to {@link MBeanNotificationInfo}.
     *
     * @param notificationType The notification type.
     * @return The MBean Notification Info
     */
    public static MBeanNotificationInfo toMBeanNotification(NotificationType<?> notificationType) {

        return new MBeanNotificationInfo(new String[] { notificationType.getName() },
                notificationType.getDataType().getName(), notificationType.getName());
    }

    /**
     * Adapt a Notification Type to a JMX Filter.
     */
    static class FilterAdaptor implements NotificationFilter, Serializable {

        private final NotificationType<?> notificationType;

        FilterAdaptor(NotificationType<?> notificationType) {
            this.notificationType = Objects.requireNonNull(notificationType);
        }

        @Override
        public boolean isNotificationEnabled(javax.management.Notification notification) {
            return notificationType.getName().equals(notification.getType());
        }
    }


    /**
     * Wraps a JMX listener as a Remote Listener
     *
     * @param <T> The type of the notification
     */
    static class RemoteListenerAdaptor<T> implements NotificationListener<T> {

        private final ObjectName objectName;

        private final javax.management.NotificationListener jmxListener;

        RemoteListenerAdaptor(ObjectName objectName, javax.management.NotificationListener jmxListener) {
            this.objectName = objectName;
            this.jmxListener = Objects.requireNonNull(jmxListener);
        }

        @Override
        public void handleNotification(Notification<T> notification) {

            jmxListener.handleNotification(RemoteBridge.toJmxNotification(objectName, notification), null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RemoteListenerAdaptor<?> that = (RemoteListenerAdaptor<?>) o;
            return Objects.equals(jmxListener, that.jmxListener);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jmxListener);
        }

    }

    /**
     * Wraps a Remote Listener as a JM Listener.
     *
     * @param <T> The type of the notification.
     */
    static class JmxListenerAdaptor<T> implements javax.management.NotificationListener {

        private final long remoteId;

        private final org.oddjob.remote.NotificationListener<T> listener;

        private final Class<T> dataType;

        JmxListenerAdaptor(long remoteId, org.oddjob.remote.NotificationListener<T> listener, Class<T> dataType) {
            this.remoteId = remoteId;
            this.listener = Objects.requireNonNull(listener);
            this.dataType = dataType;
        }

        @Override
        public void handleNotification(javax.management.Notification notification, Object handback) {
            listener.handleNotification(RemoteBridge.toRemoteNotification(remoteId, dataType, notification));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JmxListenerAdaptor<?> that = (JmxListenerAdaptor<?>) o;
            return Objects.equals(listener, that.listener);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listener);
        }

        @Override
        public String toString() {
            return "JmxListenerAdaptor{" +
                    "remoteId=" + remoteId +
                    ", listener=" + listener +
                    ", dataType=" + dataType +
                    '}';
        }
    }
}