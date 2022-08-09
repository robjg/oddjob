package org.oddjob.jmx.server;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import javax.management.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.*;

public class OddjobMBeanBareTest {

    public static class MyMBean extends NotificationBroadcasterSupport implements DynamicMBean {

        @Override
        public Object getAttribute(String attribute) throws AttributeNotFoundException {
            throw new AttributeNotFoundException();
        }

        @Override
        public void setAttribute(Attribute attribute) throws AttributeNotFoundException {
            throw new AttributeNotFoundException();

        }

        @Override
        public AttributeList getAttributes(String[] attributes) {
            throw new RuntimeException("Unexpected");
        }

        @Override
        public AttributeList setAttributes(AttributeList attributes) {
            throw new RuntimeException("Unexpected");
        }

        @Override
        public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
            throw new RuntimeException("Unexpected");
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            return new MBeanInfo(getClass().getName(),
                    "Foo Stuff",
                    new MBeanAttributeInfo[0],
                    new MBeanConstructorInfo[0],
                    new MBeanOperationInfo[0],
                    new MBeanNotificationInfo[0]);
        }

        @Override
        public MBeanNotificationInfo[] getNotificationInfo() {
            return new MBeanNotificationInfo[0];
        }
    }


    /**
     * Tracking down a bug in {@link org.oddjob.jmx.handlers.ComponentOwnerHandlerFactory} when a missing
     * notification type stopped a listener from being registered which wasn't a problem when
     * {@link OddjobMBean extends {@link NotificationBroadcasterSupport}...
     */
    @Test
    public void testNotificationsRegisteredWhenInfoMissing() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InstanceNotFoundException {

        MyMBean myMBean = new MyMBean();

        ObjectName on = new ObjectName("Oddjob:name=whatever");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        mbs.registerMBean(myMBean, on);

        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType("foo");

        List<Notification> notifications = new ArrayList<>();

        mbs.addNotificationListener(on,
                (notification, handback) -> notifications.add(notification),
                filter, null);

        Notification n1 = new Notification("foo", myMBean, 23L);
        myMBean.sendNotification(n1);

        assertThat(notifications, contains(n1));
    }

    /**
     * This works too... so now to look at {@link JmxListenerHelper}...
     */
    @Test
    public void testNotificationsRegisteredWhenInfoMissing2() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InstanceNotFoundException {

        NotificationEmitter notificationEmitter = mock(NotificationEmitter.class);

        ServerInterfaceManager serverInterfaceManager = mock(ServerInterfaceManager.class);
        when(serverInterfaceManager.getMBeanInfo()).thenReturn(new MBeanInfo(getClass().getName(),
                "Foo Stuff",
                new MBeanAttributeInfo[0],
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[0],
                new MBeanNotificationInfo[0]));

        OddjobMBean myMBean = OddjobMBean.create(new Object(), 1L,
                mock(ServerSession.class),
                serverInterfaceManager,
                notificationEmitter);

        ObjectName on = new ObjectName("Oddjob:name=whatever");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        mbs.registerMBean(myMBean, on);

        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType("foo");

        List<Notification> notifications = new ArrayList<>();

        mbs.addNotificationListener(on,
                (notification, handback) -> notifications.add(notification),
                filter, null);

        ArgumentCaptor<NotificationListener> listenerCaptor = ArgumentCaptor.forClass(NotificationListener.class);

        verify(notificationEmitter, times(1))
                .addNotificationListener(listenerCaptor.capture(),
                        ArgumentMatchers.any(NotificationFilter.class),
                        ArgumentMatchers.nullable(Object.class));

        Notification n1 = new Notification("foo", myMBean, 23L);

        NotificationListener listener = listenerCaptor.getValue();

        listener.handleNotification(n1, null);

        assertThat(notifications, contains(n1));
    }
}
