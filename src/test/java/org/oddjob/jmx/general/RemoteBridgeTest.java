package org.oddjob.jmx.general;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jmx.handlers.StatefulHandlerFactory;
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
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.oddjob.state.GenericState.statesEquivalent;

public class RemoteBridgeTest {

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

        assertThat(statesEquivalent(stateData.getJobState(), JobState.READY),
                is(true));

        remoteBridge.invoke(1, OperationType.ofName("run").returningVoid());

        StatefulHandlerFactory.StateData stateData2 =
                results.poll(2, TimeUnit.SECONDS).getData();

        assertThat(statesEquivalent(stateData2.getJobState(), JobState.EXECUTING),
                is(true));

        StatefulHandlerFactory.StateData stateData3 =
                results.poll(2, TimeUnit.SECONDS).getData();

        assertThat(statesEquivalent(stateData3.getJobState(), JobState.COMPLETE),
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
                removeArg1.capture(), removeArg2.capture(), any(), any());

        assertThat(removeArg2.getValue(), is(jmxListener));
    }

    @Test
    public void givenTwoListenersWhenBothRemovedNothingReceived() throws RemoteException, IOException, InstanceNotFoundException, ListenerNotFoundException {

        MBeanServerConnection mBeanServerConnection = mock(MBeanServerConnection.class);

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
                any(), removeArg2.capture(), any(), any());

        assertThat(removeArg2.getAllValues().size(), is(2));

        assertThat(removeArg2.getAllValues().get(0), is(jmxListener1));
        assertThat(removeArg2.getAllValues().get(1), is(jmxListener2));
    }

    @Test
    public void givenTwoListenersOfDifferentTypesWhenBothRemovedNothingReceived() throws RemoteException, IOException, InstanceNotFoundException, ListenerNotFoundException {

        MBeanServerConnection mBeanServerConnection = mock(MBeanServerConnection.class);

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

        ArgumentCaptor<javax.management.NotificationFilter> removeArg3 =
                ArgumentCaptor.forClass(javax.management.NotificationFilter.class);

        verify(mBeanServerConnection, times(2)).removeNotificationListener(
                any(ObjectName.class), removeArg2.capture(), removeArg3.capture(), Mockito.isNull());

        assertThat(removeArg2.getAllValues().size(), is(2));

        assertThat(removeArg2.getAllValues().get(0), is(jmxListener1));
        assertThat(removeArg2.getAllValues().get(1), is(jmxListener2));

        assertThat(removeArg3.getAllValues().get(0), is(arg3.getAllValues().get(0)));
        assertThat(removeArg3.getAllValues().get(1), is(arg3.getAllValues().get(1)));
    }

    @Test
    public void whenListenerRegisteredTwiceExceptionIsThrown() throws RemoteException {

        MBeanServerConnection mBeanServerConnection = mock(MBeanServerConnection.class);

        RemoteBridge remoteBridge = new RemoteBridge(mBeanServerConnection);

        NotificationType<String> notificationType1 = NotificationType.ofName("foo")
                .andDataType(String.class);

        NotificationListener<String> listener1 = mock(NotificationListener.class);

        // Add

        remoteBridge.addNotificationListener(42L, notificationType1, listener1);

        try {
            remoteBridge.addNotificationListener(42L, notificationType1, listener1);
            fail("Exception Expected");
        }
        catch (RemoteIdException e) {
            assertThat(e.getMessage(), Matchers.containsString("Listener"));
        }
    }

}