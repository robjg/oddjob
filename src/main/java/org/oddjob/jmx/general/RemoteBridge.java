package org.oddjob.jmx.general;

import org.oddjob.arooa.utils.ClassUtils;
import org.oddjob.arooa.utils.Pair;
import org.oddjob.jmx.server.OddjobMBeanFactory;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.*;

import javax.management.*;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridge between the Oddjob Generic Remote API and JMX remoting.
 */
public class RemoteBridge implements RemoteConnection {

    private final MBeanServerConnection mbsc;

    private final Map<Pair<Long, Pair<?, ?>>,
            Pair<NotificationFilter, javax.management.NotificationListener>> listeners =
            new ConcurrentHashMap<>();

    public RemoteBridge(MBeanServerConnection mbsc) {
        this.mbsc = Objects.requireNonNull(mbsc);
    }

    @Override
    public <T> void addNotificationListener(long remoteId,
                                            NotificationType<T> notificationType,
                                            NotificationListener<T> notificationListener)
            throws RemoteException {

        Pair<Long, Pair<?, ?>> listenerKey = Pair.of(remoteId, Pair.of(notificationType, notificationListener));

        if (listeners.containsKey(listenerKey)) {
            throw new RemoteIdException(remoteId, "Listener " + notificationListener + " " +
                    "already registered for notification type " + notificationType);
        }

        javax.management.NotificationListener jmxListener = (notification, handback) ->
                notificationListener.handleNotification(
                        fromJmxNotification(remoteId, notificationType.getDataType(), notification));

        NotificationFilter filter = createTypeFilterFor(notificationType);

        try {
            mbsc.addNotificationListener(OddjobMBeanFactory.objectName(remoteId),
                    jmxListener,
                    filter,
                    null);
        } catch (InstanceNotFoundException | IOException e) {
            throw new RemoteIdException(remoteId, e);
        }

        listeners.put(listenerKey, Pair.of(filter, jmxListener));
    }

    @Override
    public <T> void removeNotificationListener(long remoteId,
                                               NotificationType<T> notificationType,
                                               NotificationListener<T> notificationListener)
            throws RemoteException {

        Pair<NotificationFilter, javax.management.NotificationListener> pair =
                listeners.remove(Pair.of(remoteId, Pair.of(notificationType, notificationListener)));

        if (pair == null) {
            throw new RemoteIdException(remoteId, "Listener " + notificationListener +
                    " not registered for " + notificationType);
        }

        try {
            mbsc.removeNotificationListener(OddjobMBeanFactory.objectName(remoteId),
                    pair.getRight(),
                    pair.getLeft(),
                    null);
        } catch (InstanceNotFoundException | ListenerNotFoundException | IOException e) {
            throw new RemoteIdException(remoteId, e);
        }
    }

    @Override
    public <T> T invoke(long remoteId, OperationType<T> operationType, Object... args) throws RemoteException {

        String[] signature = ClassUtils.classesToStrings(operationType.getSignature());

        Object result;
        try {
            result = mbsc.invoke(OddjobMBeanFactory.objectName(remoteId),
                    operationType.getName(),
                    args,
                    signature);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | IOException e) {
            throw new RemoteIdException(remoteId, e);
        }

        return ClassUtils.cast(operationType.getReturnType(), result);
    }

    public static NotificationFilter createTypeFilterFor(NotificationType<?> type) {
        NotificationFilterSupport typeFilter = new NotificationFilterSupport();
        typeFilter.enableType(type.getName());
        return typeFilter;
    }

    @SuppressWarnings("unchecked")
    public static <T> Notification<T> fromJmxNotification(long remoteId,
                                                          Class<T> dataType,
                                                          javax.management.Notification notification) {
        return new Notification<>(remoteId,
                new NotificationType<>(notification.getType(), dataType),
                notification.getSequenceNumber(),
                (T) notification.getUserData());
    }


    public static javax.management.Notification toJmxNotification(ObjectName objectName,
                                                                  Notification<?> notification) {
        javax.management.Notification jmxNotification =
                new javax.management.Notification(notification.getType().getName(),
                        objectName,
                        notification.getSequence());
        jmxNotification.setUserData(notification.getData());
        return jmxNotification;
    }
}
