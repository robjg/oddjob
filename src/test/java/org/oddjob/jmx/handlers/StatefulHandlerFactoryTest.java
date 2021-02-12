/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.jmx.handlers;

import org.junit.Test;
import org.oddjob.MockStateful;
import org.oddjob.OjTestCase;
import org.oddjob.Stateful;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;
import org.oddjob.state.GenericState;
import org.oddjob.state.JobState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

import static org.hamcrest.Matchers.is;

public class StatefulHandlerFactoryTest extends OjTestCase {

    private static class OurStateful extends MockStateful {
        StateListener l;

        public void addStateListener(StateListener listener) {
            assertNull(l);
            l = listener;
            l.jobStateChange(new StateEvent(this, JobState.READY));
        }

        public void removeStateListener(StateListener listener) {
            assertNotNull(l);
            l = null;
        }
    }

    private static class OurClientToolkit extends MockClientSideToolkit {
        ServerInterfaceHandler server;

        NotificationListener listener;

        @SuppressWarnings("unchecked")
        @Override
        public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
                throws Throwable {
            return (T) server.invoke(remoteOperation, args);
        }

        @Override
        public <T> void registerNotificationListener(NotificationType<T> eventType,
                                                     NotificationListener<T> notificationListener) {
            if (listener != null) {
                throw new RuntimeException("Only one listener expected.");
            }
            assertEquals(StatefulHandlerFactory.STATE_CHANGE_NOTIF_TYPE, eventType);

            this.listener = notificationListener;
        }

        @Override
        public <T> void removeNotificationListener(NotificationType<T> eventType,
                                               NotificationListener<T> notificationListener) {
            if (listener == null) {
                throw new RuntimeException("Only one listener remove expected.");
            }

            assertEquals(StatefulHandlerFactory.STATE_CHANGE_NOTIF_TYPE, eventType);
            assertEquals(this.listener, notificationListener);

            this.listener = null;
        }
    }

    private static class OurServerSideToolkit extends MockServerSideToolkit {

        long seq = 0;

        NotificationListener listener;

        public void runSynchronized(Runnable runnable) {
            runnable.run();
        }

        @Override
        public <T> Notification<T> createNotification(NotificationType<T> type, T userData) {
            return new Notification<>(1L, type, seq++, userData);
        }

        public void sendNotification(Notification<?> notification) {
            if (listener != null) {
                listener.handleNotification(notification);
            }
        }

    }

    private static class Result implements StateListener {
        StateEvent event;

        public void jobStateChange(StateEvent event) {
            this.event = event;
        }
    }

    @Test
    public void testAddRemoveListener() {

        StatefulHandlerFactory test = new StatefulHandlerFactory();

        assertEquals(1, test.getNotificationTypes().size());

        OurStateful stateful = new OurStateful();
        OurServerSideToolkit serverToolkit = new OurServerSideToolkit();

        // create the handler
        ServerInterfaceHandler serverHandler = test.createServerHandler(
                stateful, serverToolkit);

        // which should add a listener to our stateful
        assertNotNull("listener added.", stateful.l);

        OurClientToolkit clientToolkit = new OurClientToolkit();

        Stateful local = new StatefulHandlerFactory.ClientFactory(
        ).createClientHandler(new MockStateful(), clientToolkit);

        clientToolkit.server = serverHandler;

        Result result = new Result();

        local.addStateListener(result);

        assertThat("State ready",
                GenericState.statesEquivalent(JobState.READY, result.event.getState()),
                is(true));

        Result result2 = new Result();

        local.addStateListener(result2);

        assertThat("State ready",
                GenericState.statesEquivalent(JobState.READY, result2.event.getState()),
                is(true));

        serverToolkit.listener = clientToolkit.listener;

        stateful.l.jobStateChange(new StateEvent(stateful, JobState.COMPLETE));

        // check the notification is sent
        assertThat("State complete",
                GenericState.statesEquivalent(JobState.COMPLETE, result.event.getState()),
                is(true));
        assertThat("State complete",
                GenericState.statesEquivalent(JobState.COMPLETE, result2.event.getState()),
                is(true));

        local.removeStateListener(result);

        assertNotNull(clientToolkit.listener);

        local.removeStateListener(result2);

        assertNull(clientToolkit.listener);

    }

    private static class OurClientToolkit2 extends MockClientSideToolkit {
        ServerInterfaceHandler server;

        @SuppressWarnings("unchecked")
        @Override
        public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
                throws Throwable {
            return (T) server.invoke(remoteOperation, args);
        }
    }

    @Test
    public void testLastStateEventCallsRemoteOpWhenNoListenerAdded() {

        StatefulHandlerFactory test = new StatefulHandlerFactory();

//		assertEquals(1, test.getMBeanOperationInfo().length);

        OurStateful stateful = new OurStateful();
        OurServerSideToolkit serverToolkit = new OurServerSideToolkit();

        // create the handler
        ServerInterfaceHandler serverHandler = test.createServerHandler(
                stateful, serverToolkit);

        OurClientToolkit2 clientToolkit = new OurClientToolkit2();

        Stateful local = new StatefulHandlerFactory.ClientFactory(
        ).createClientHandler(new MockStateful(), clientToolkit);

        clientToolkit.server = serverHandler;

        stateful.l.jobStateChange(new StateEvent(stateful, JobState.COMPLETE));

        StateEvent lastStateEvent = local.lastStateEvent();

        assertThat(GenericState.statesEquivalent(JobState.COMPLETE,
                lastStateEvent.getState()), is(true));

    }


}
