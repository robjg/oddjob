package org.oddjob.jmx.general;

import org.oddjob.remote.Notification;

import javax.management.ObjectName;

/**
 * Bridge between the Oddjob Generic Remote API and JMX remoting.
 */
public class RemoteBridge {

    public static Notification fromJmxNotification(long remoteId,
                                                   javax.management.Notification notification) {
        return new Notification(remoteId, notification.getType(),
                notification.getSequenceNumber(),
                notification.getUserData());
    }

    public static javax.management.Notification toJmxNotification(ObjectName objectName,
                                                                  Notification notification) {
        javax.management.Notification jmxNotification =
                new javax.management.Notification(notification.getType(),
                        objectName,
                        notification.getSequence());
        jmxNotification.setUserData(notification.getData());
        return jmxNotification;
    }
}
