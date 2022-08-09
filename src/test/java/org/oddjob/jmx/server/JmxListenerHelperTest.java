package org.oddjob.jmx.server;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.oddjob.jmx.JMXAssumptionsTest;
import org.oddjob.jmx.general.RemoteBridge;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.util.NotifierListener;
import org.oddjob.remote.util.NotifierListenerEvent;

import javax.management.*;
import javax.management.remote.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

public class JmxListenerHelperTest {


    @Test
    public void testAddRemoveListener() throws MalformedObjectNameException, ListenerNotFoundException {

        NotificationType<String> type1 =
                NotificationType.ofName("some.string.event")
                        .andDataType(String.class);

        NotificationType<Integer> type2 =
                NotificationType.ofName("some.integer.event")
                        .andDataType(Integer.class);

        ObjectName objectName = new ObjectName("foo:name=foo");

        JmxListenerHelper test = new JmxListenerHelper(objectName);
        test.setNotificationTypes(new HashSet<>(Arrays.asList(type1, type2)));

        @SuppressWarnings("unchecked")
        NotifierListener<String> notifierListener = mock(NotifierListener.class);

        test.setNotifierListener(type1, notifierListener);

        NotificationListener jmxListener = mock(NotificationListener.class);

        // Add

        // needed for RMI
        String handback = "HandBack";

        test.addNotificationListener(jmxListener, RemoteBridge.createTypeFilterFor(type1), handback);

        verify(notifierListener, times(1))
                .notificationListenerAdded(new NotifierListenerEvent<>(
                        type1, RemoteBridge.toRemoteListener(objectName, jmxListener)));

        // Send

        Notification<String> notification = new Notification<>(
                42L, type1, 1000L, "Apple");

        test.sendNotification(notification);

        ArgumentCaptor<javax.management.Notification> notificationArgumentCaptor
                = ArgumentCaptor.forClass(javax.management.Notification.class);
        ArgumentCaptor<Object> handbackArgumentCaptor
                = ArgumentCaptor.forClass(Object.class);

        verify(jmxListener, times(1)).handleNotification(
                notificationArgumentCaptor.capture(), handbackArgumentCaptor.capture());

        assertThat(notificationArgumentCaptor.getValue().getUserData(), is("Apple"));
        assertThat(handbackArgumentCaptor.getValue(), is("HandBack"));

        // Remove

        test.removeNotificationListener(jmxListener);

        verify(notifierListener, times(1))
                .notificationListenerRemoved(new NotifierListenerEvent<>(
                        type1, RemoteBridge.toRemoteListener(objectName, jmxListener)));

        // Send another event

        test.sendNotification(notification);

        verifyNoMoreInteractions(jmxListener);
    }


    private static class OurMBean implements DynamicMBean, NotificationEmitter  {

        private final ObjectName objectName;

        private final JmxListenerHelper listenerHelper;

        OurMBean(List<NotificationType<?>> notificationTypes) throws MalformedObjectNameException {
            this.objectName = ObjectName.getInstance("foo:name=bar");
            this.listenerHelper = new JmxListenerHelper(this.objectName);

            this.listenerHelper.setNotificationTypes(new HashSet<>(notificationTypes));
        }

        @Override
        public Object getAttribute(String attribute) {
            throw new RuntimeException("Unexpected");
        }

        @Override
        public AttributeList getAttributes(String[] attributes) {
            throw new RuntimeException("Unexpected");
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            return new MBeanInfo(JMXAssumptionsTest.OurMBean.class.getName(),
                    "Test",
                    new MBeanAttributeInfo[0],
                    new MBeanConstructorInfo[0],
                    new MBeanOperationInfo[0],
                    listenerHelper.getNotificationInfo());
        }

        @Override
        public Object invoke(String actionName, Object[] params,
                             String[] signature) throws MBeanException, ReflectionException {
            throw new RuntimeException("Unexpected");
        }

        @Override
        public void setAttribute(Attribute attribute) {
            throw new RuntimeException("Unexpected");
        }

        @Override
        public AttributeList setAttributes(AttributeList attributes) {
            throw new RuntimeException("Unexpected");
        }

        @Override
        public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException {
            listenerHelper.removeNotificationListener(listener, filter, handback);
        }

        @Override
        public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException {
            listenerHelper.addNotificationListener(listener, filter, handback);
        }

        @Override
        public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
            listenerHelper.removeNotificationListener(listener);
        }

        @Override
        public MBeanNotificationInfo[] getNotificationInfo() {
            return listenerHelper.getNotificationInfo();
        }
    }

    @Test
    public void testOverRmi() throws Exception {

        JMXServiceURL address = new JMXServiceURL("service:jmx:rmi://");

        MBeanServer server = MBeanServerFactory.createMBeanServer();

        JMXConnectorServer cntorServer = JMXConnectorServerFactory.newJMXConnectorServer(
                address, null, server);
        cntorServer.start();

        NotificationType<String> type1 = NotificationType.ofName("test1")
                .andDataType(String.class);
        NotificationType<String> type2 = NotificationType.ofName("test2")
                .andDataType(String.class);

        final OurMBean ourMBean = new OurMBean(Arrays.asList(type1, type2));

        server.registerMBean(ourMBean, ourMBean.objectName);

        JMXConnector cntor = JMXConnectorFactory.connect(
                cntorServer.getAddress());

        MBeanServerConnection mbsc = cntor.getMBeanServerConnection();

        LinkedBlockingQueue<javax.management.Notification> notifications = new LinkedBlockingQueue<>();

        NotificationListener listener = (notification, handback) -> notifications.add(notification);

        mbsc.addNotificationListener(ourMBean.objectName, listener, null, null);

        Notification<String> notification = new Notification<>(
                42L, type1, 1000L, "Apple");

        ourMBean.listenerHelper.sendNotification(notification);

        javax.management.Notification received1 = notifications.poll(2, TimeUnit.SECONDS);

        assertThat(received1, notNullValue());

        mbsc.removeNotificationListener(ourMBean.objectName, listener);

        cntor.close();
        cntorServer.stop();
    }

    @Test
    public void whenTypeNotInInfoThenCheckWhatHappens() throws MalformedObjectNameException {

        NotificationType<String> type1 =
                NotificationType.ofName("some.string.event")
                        .andDataType(String.class);

        ObjectName objectName = new ObjectName("foo:name=foo");

        JmxListenerHelper test = new JmxListenerHelper(objectName);

        NotificationListener jmxListener = mock(NotificationListener.class);

        // Add

        test.addNotificationListener(jmxListener, RemoteBridge.createTypeFilterFor(type1), null);

        // Send

        Notification<String> notification = new Notification<>(
                42L, type1, 1000L, "Apple");

        test.sendNotification(notification);

        verifyNoInteractions(jmxListener);

        // Register Type

        test.setNotificationTypes(new HashSet<>(Collections.singletonList(type1)));

        // Add again

        test.addNotificationListener(jmxListener, RemoteBridge.createTypeFilterFor(type1), null);

        // And send again

        test.sendNotification(notification);

        // Verify

        ArgumentCaptor<javax.management.Notification> notificationArgumentCaptor
                = ArgumentCaptor.forClass(javax.management.Notification.class);

        verify(jmxListener, times(1)).handleNotification(
                notificationArgumentCaptor.capture(), ArgumentMatchers.nullable(Object.class));

        assertThat(notificationArgumentCaptor.getValue().getUserData(), is("Apple"));
    }
}