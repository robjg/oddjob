package org.oddjob.jmx.general;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jmx.handlers.StatefulHandlerFactory;
import org.oddjob.jmx.server.OddjobMBeanFactory;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.*;
import org.oddjob.state.JobState;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.oddjob.state.GenericState.statesEquivalent;

public class RemoteBridgeTest {

    @Test
    public void testToJmxNotification() throws MalformedObjectNameException {

        NotificationType<String> type1 =
                NotificationType.ofName("some.string.event")
                        .andDataType(String.class);

        Notification<String> remoteNotification = new Notification<>(
                42L, type1, 1000L, "Apple");

        ObjectName objectName = new ObjectName("foo:name=foo");

        javax.management.Notification jmxNotification =
                RemoteBridge.toJmxNotification(objectName, remoteNotification);

        assertThat(jmxNotification.getType(), is("some.string.event"));
        assertThat(jmxNotification.getUserData(), is("Apple"));
        assertThat(jmxNotification.getSequenceNumber(), is(1000L));
        assertThat(jmxNotification.getSource(), is(objectName));
    }

    @Test
    public void testToJmxListenerListenersEqualsForSameRemoteListener() {

        NotificationListener<String> remoteListener = mock(NotificationListener.class);

        javax.management.NotificationListener jmxListener1 = RemoteBridge.toJmxListener(
                42L, remoteListener, String.class);

        javax.management.NotificationListener jmxListener2 = RemoteBridge.toJmxListener(
                42L, remoteListener, String.class);

        assertThat(jmxListener1, is(jmxListener2));
    }

    @Test
    public void tesIisFilterFor() {

        NotificationType<String> type1 =
                NotificationType.ofName("some.string.event")
                        .andDataType(String.class);

        NotificationType<String> type2 =
                NotificationType.ofName("some.other.event")
                        .andDataType(String.class);

        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType("some.string.event");

        assertThat(RemoteBridge.isFilterForType(filter, type1), is(true));
        assertThat(RemoteBridge.isFilterForType(filter, type2), is(false));
    }

    @Test
    public void testAgainstJmxServer() throws RemoteException, FailedToStopException, InterruptedException {

        Oddjob server = new Oddjob();
        server.setConfiguration(new XMLConfiguration(
                "org/oddjob/jmx/PlatformMBeanServerExample.xml",
                getClass().getClassLoader()));

        server.run();

        RemoteBridge remoteBridge = new RemoteBridge(
                ManagementFactory.getPlatformMBeanServer());

        BlockingQueue<Notification<StatefulHandlerFactory.StateData>> results = new LinkedBlockingDeque<>();

        NotificationListener<StatefulHandlerFactory.StateData> listener =
                results::add;

        remoteBridge.addNotificationListener(1L,
                StatefulHandlerFactory.STATE_CHANGE_NOTIF_TYPE,
                listener);

        remoteBridge.invoke(1, OperationType.ofName("hardReset").returningVoid());

        StatefulHandlerFactory.StateData stateData =
                results.poll(2, TimeUnit.SECONDS).getData();

        assertThat(statesEquivalent(stateData.getState(), JobState.READY),
                is(true));

        remoteBridge.invoke(1, OperationType.ofName("run").returningVoid());

        StatefulHandlerFactory.StateData stateData2 =
                results.poll(2, TimeUnit.SECONDS).getData();

        assertThat(statesEquivalent(stateData2.getState(), JobState.EXECUTING),
                is(true));

        StatefulHandlerFactory.StateData stateData3 =
                results.poll(2, TimeUnit.SECONDS).getData();

        assertThat(statesEquivalent(stateData3.getState(), JobState.COMPLETE),
                is(true));

        Object text = remoteBridge.invoke(1,
                OperationType.ofName("get")
                        .withSignature(String.class).returning(Object.class),
                "text");

        assertThat(text, is("Hello from an Oddjob Server!"));

        remoteBridge.removeNotificationListener(1L,
                StatefulHandlerFactory.STATE_CHANGE_NOTIF_TYPE,
                listener);

        server.stop();
    }

    @Test
    public void testAddNotifyRemove() throws RemoteException, IOException, InstanceNotFoundException, MalformedObjectNameException, ListenerNotFoundException {

        MBeanServerConnection mBeanServerConnection = mock(MBeanServerConnection.class);
        when(mBeanServerConnection.isRegistered(OddjobMBeanFactory.objectName(42)))
                .thenReturn(true);

        RemoteBridge remoteBridge = new RemoteBridge(mBeanServerConnection);

        NotificationType<String> notificationType = NotificationType.ofName("foo")
                .andDataType(String.class);

        NotificationListener<String> l = mock(NotificationListener.class);

        // Add

        remoteBridge.addNotificationListener(42L, notificationType, l);

        ArgumentCaptor<ObjectName> arg1 = ArgumentCaptor.forClass(ObjectName.class);
        ArgumentCaptor<javax.management.NotificationListener> arg2 =
                ArgumentCaptor.forClass(javax.management.NotificationListener.class);

        verify(mBeanServerConnection, times(1)).addNotificationListener(
                arg1.capture(), arg2.capture(), any(), any());

        assertThat(arg1.getValue(), is(new ObjectName("oddjob", "uid", "00000042")));

        // Send

        javax.management.NotificationListener jmxListener = arg2.getValue();

        javax.management.Notification n1 = new javax.management.Notification(
                "foo", new Object(), 1000L);
        n1.setUserData("Some Foo");

        jmxListener.handleNotification(n1, null);

        verify(l, times(1)).handleNotification(
                new Notification<>(42, notificationType, 1000L, "Some Foo"));

        // Remove

        remoteBridge.removeNotificationListener(42L, notificationType, l);

        ArgumentCaptor<ObjectName> removeArg1 = ArgumentCaptor.forClass(ObjectName.class);
        ArgumentCaptor<javax.management.NotificationListener> removeArg2 =
                ArgumentCaptor.forClass(javax.management.NotificationListener.class);

        verify(mBeanServerConnection, times(1)).removeNotificationListener(
                removeArg1.capture(), removeArg2.capture());

        assertThat(removeArg2.getValue(), is(jmxListener));
    }

    @Test
    public void givenTwoListenersWhenBothRemovedNothingReceived() throws RemoteException, IOException, InstanceNotFoundException, ListenerNotFoundException {

        MBeanServerConnection mBeanServerConnection = mock(MBeanServerConnection.class);
        when(mBeanServerConnection.isRegistered(OddjobMBeanFactory.objectName(42)))
                .thenReturn(true);

        RemoteBridge remoteBridge = new RemoteBridge(mBeanServerConnection);

        NotificationType<String> notificationType = NotificationType.ofName("foo")
                .andDataType(String.class);

        NotificationListener<String> listener1 = mock(NotificationListener.class);
        NotificationListener<String> listener2 = mock(NotificationListener.class);

        // Add

        remoteBridge.addNotificationListener(42L, notificationType, listener1);
        remoteBridge.addNotificationListener(42L, notificationType, listener2);

        ArgumentCaptor<javax.management.NotificationListener> arg2 =
                ArgumentCaptor.forClass(javax.management.NotificationListener.class);

        verify(mBeanServerConnection, times(2)).addNotificationListener(
                any(), arg2.capture(), any(), any());

        // Check Different

        assertThat(arg2.getAllValues().size(), is(2));

        javax.management.NotificationListener jmxListener1 = arg2.getAllValues().get(0);
        javax.management.NotificationListener jmxListener2 = arg2.getAllValues().get(1);

        assertThat(jmxListener1, not(jmxListener2));

        // Remove

        remoteBridge.removeNotificationListener(42L, notificationType, listener1);
        remoteBridge.removeNotificationListener(42L, notificationType, listener2);

        ArgumentCaptor<javax.management.NotificationListener> removeArg2 =
                ArgumentCaptor.forClass(javax.management.NotificationListener.class);

        verify(mBeanServerConnection, times(2)).removeNotificationListener(
                any(), removeArg2.capture());

        assertThat(removeArg2.getAllValues().size(), is(2));

        assertThat(removeArg2.getAllValues().get(0), is(jmxListener1));
        assertThat(removeArg2.getAllValues().get(1), is(jmxListener2));
    }

    @Test
    public void givenTwoListenersOfDifferentTypesWhenBothRemovedNothingReceived() throws RemoteException, IOException, InstanceNotFoundException, ListenerNotFoundException {

        MBeanServerConnection mBeanServerConnection = mock(MBeanServerConnection.class);
        when(mBeanServerConnection.isRegistered(OddjobMBeanFactory.objectName(42)))
            .thenReturn(true);

        RemoteBridge remoteBridge = new RemoteBridge(mBeanServerConnection);

        NotificationType<String> notificationType1 = NotificationType.ofName("foo")
                .andDataType(String.class);
        NotificationType<String> notificationType2 = NotificationType.ofName("bar")
                .andDataType(String.class);

        NotificationListener<String> listener1 = mock(NotificationListener.class);
        NotificationListener<String> listener2 = mock(NotificationListener.class);

        // Add

        remoteBridge.addNotificationListener(42L, notificationType1, listener1);
        remoteBridge.addNotificationListener(42L, notificationType2, listener2);

        ArgumentCaptor<javax.management.NotificationListener> arg2 =
                ArgumentCaptor.forClass(javax.management.NotificationListener.class);

        ArgumentCaptor<javax.management.NotificationFilter> arg3 =
                ArgumentCaptor.forClass(javax.management.NotificationFilter.class);

        verify(mBeanServerConnection, times(2)).addNotificationListener(
                any(ObjectName.class), arg2.capture(), arg3.capture(), Mockito.isNull());

        // Check Different

        assertThat(arg2.getAllValues().size(), is(2));

        javax.management.NotificationListener jmxListener1 = arg2.getAllValues().get(0);
        javax.management.NotificationListener jmxListener2 = arg2.getAllValues().get(1);

        assertThat(jmxListener1, not(jmxListener2));

        // Remove

        remoteBridge.removeNotificationListener(42L, notificationType1, listener1);
        remoteBridge.removeNotificationListener(42L, notificationType2, listener2);

        ArgumentCaptor<javax.management.NotificationListener> removeArg2 =
                ArgumentCaptor.forClass(javax.management.NotificationListener.class);

        verify(mBeanServerConnection, times(2)).removeNotificationListener(
                any(ObjectName.class), removeArg2.capture());

        assertThat(removeArg2.getAllValues().size(), is(2));

        assertThat(removeArg2.getAllValues().get(0), is(jmxListener1));
        assertThat(removeArg2.getAllValues().get(1), is(jmxListener2));
    }

    @Test
    public void whenDestroyThenListenersRemoved() throws IOException, RemoteException,
            InstanceNotFoundException, ListenerNotFoundException {

        MBeanServerConnection mBeanServerConnection = mock(MBeanServerConnection.class);
        when(mBeanServerConnection.isRegistered(OddjobMBeanFactory.objectName(42)))
                .thenReturn(true);

        RemoteBridge remoteBridge = new RemoteBridge(mBeanServerConnection);

        NotificationType<String> notificationType1 = NotificationType.ofName("foo")
                .andDataType(String.class);
        NotificationType<String> notificationType2 = NotificationType.ofName("bar")
                .andDataType(String.class);

        NotificationListener<String> listener1 = mock(NotificationListener.class);

        // Add

        remoteBridge.addNotificationListener(42L, notificationType1, listener1);

        ArgumentCaptor<javax.management.NotificationListener> arg2 =
                ArgumentCaptor.forClass(javax.management.NotificationListener.class);

        verify(mBeanServerConnection, times(1)).addNotificationListener(
                any(ObjectName.class), arg2.capture(),
                any(javax.management.NotificationFilter.class), Mockito.isNull());

        // When

        remoteBridge.destroy(42L);

        verify(mBeanServerConnection, times(1)).removeNotificationListener(
                any(ObjectName.class), eq(arg2.getValue()));
    }
}