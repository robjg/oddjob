package org.oddjob.jmx.general;

import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jmx.handlers.StatefulHandlerFactory;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.OperationType;
import org.oddjob.remote.RemoteException;
import org.oddjob.state.JobState;

import java.lang.management.ManagementFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.oddjob.state.GenericState.statesEquivalent;

public class RemoteBridgeTest {

    @Test
    public void testInvoke() throws RemoteException, FailedToStopException, InterruptedException {

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



}