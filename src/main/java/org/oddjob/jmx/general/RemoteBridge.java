package org.oddjob.jmx.general;

import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationType;

import javax.management.ObjectName;

/**
 * Bridge between the Oddjob Generic Remote API and JMX remoting.
 */
public class RemoteBridge {

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
