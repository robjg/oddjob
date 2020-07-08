package org.oddjob.jmx.general;

import org.oddjob.arooa.utils.ClassUtils;
import org.oddjob.jmx.server.OddjobMBeanFactory;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.*;

import javax.management.*;
import java.io.IOException;
import java.util.Objects;

/**
 * Bridge between the Oddjob Generic Remote API and JMX remoting.
 */
public class RemoteBridge implements RemoteConnection {

    private final MBeanServerConnection mbsc;


    public RemoteBridge(MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
    }

    @Override
    public <T> void addNotificationListener(long remoteId,
                                            NotificationType<T> notificationType,
                                            NotificationListener<T> notificationListener)
            throws RemoteException {
        try {
            mbsc.addNotificationListener(OddjobMBeanFactory.objectName(remoteId),
                    new ListenerBridge<>(remoteId, notificationType.getDataType(), notificationListener),
                    createTypeFilterFor(notificationType), null);
        } catch (InstanceNotFoundException | IOException e) {
            throw new RemoteIdException(remoteId, e);
        }
    }

    @Override
    public <T> void removeNotificationListener(long remoteId,
                                               NotificationType<T> notificationType,
                                               NotificationListener<T> notificationListener)
            throws RemoteException {

        try {
            mbsc.removeNotificationListener(OddjobMBeanFactory.objectName(remoteId),
                    new ListenerBridge<>(remoteId, notificationType.getDataType(), notificationListener),
                    createTypeFilterFor(notificationType),
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

    static class ListenerBridge<T> implements javax.management.NotificationListener {

        private final long remoteId;

        private final Class<T> dataType;

        private final NotificationListener<T> remoteListener;

        ListenerBridge(long remoteId,
                       Class<T> dataType, NotificationListener<T> remoteListener) {
            this.remoteId = remoteId;
            this.dataType = dataType;
            this.remoteListener = remoteListener;
        }

        @Override
        public void handleNotification(javax.management.Notification notification, Object handback) {
            remoteListener.handleNotification(fromJmxNotification(remoteId, dataType, notification));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ListenerBridge<?> that = (ListenerBridge<?>) o;
            return remoteListener.equals(that.remoteListener);
        }

        @Override
        public int hashCode() {
            return Objects.hash(remoteListener);
        }
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
