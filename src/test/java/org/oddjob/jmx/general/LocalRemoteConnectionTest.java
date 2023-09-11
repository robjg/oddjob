package org.oddjob.jmx.general;

import org.junit.jupiter.api.Test;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.registry.BeanDirectoryOwner;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.RemoteDirectoryOwner;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.handlers.BeanDirectoryHandlerFactory;
import org.oddjob.jmx.handlers.RemoteOddjobHandlerFactory;
import org.oddjob.jmx.handlers.RunnableHandlerFactory;
import org.oddjob.jmx.server.ServerInfo;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerLoopBackException;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.remote.OperationType;
import org.oddjob.remote.RemoteConnection;
import org.oddjob.remote.RemoteException;
import org.oddjob.remote.util.NotificationControl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

class LocalRemoteConnectionTest {

    @Test
    void serverMainBeanImplementsExpectedInterfaces() throws RemoteException, ServerLoopBackException {

        ServerSideToolkit serverSideToolkit = mock(ServerSideToolkit.class);

        Object component = "FOO";

        NotificationControl notificationControl = mock(NotificationControl.class);

        ServerInterfaceHandlerFactory<Object, RemoteOddjobBean> remoteOddjobHandler =
                new RemoteOddjobHandlerFactory();
        ServerInterfaceHandlerFactory<BeanDirectoryOwner, RemoteDirectoryOwner> beanDirectoryHandler =
                new BeanDirectoryHandlerFactory();

        ArooaSession session = new StandardArooaSession();

        session.getBeanRegistry().register("foo", component);

        try (RemoteConnection test = LocalRemoteConnection.with(session, notificationControl)
                .setServerId("TEST-SERVER")
                .setExecutor(Runnable::run)
                .addHandlerFactory(remoteOddjobHandler)
                .addHandlerFactory(beanDirectoryHandler)
                .remoteForRoot(component)) {

            OperationType<ServerInfo> operationType = OperationType
                    .ofName(RemoteOddjobHandlerFactory.SERVER_INFO.getActionName())
                    .returning(ServerInfo.class);

            ServerInfo serverInfo = test.invoke(0L, operationType);

            assertThat(serverInfo.getImplementations().length, is(2));

            BeanDirectoryHandlerFactory.Carrier found = (BeanDirectoryHandlerFactory.Carrier)
                    test.invoke(0L, BeanDirectoryHandlerFactory.LOOKUP.getOperationType(), "foo");

            assertThat(found.getRemoteId(), is(1L));
        }
    }

    @Test
    void simpleRunnable() throws RemoteException, ServerLoopBackException {

        ServerSideToolkit serverSideToolkit = mock(ServerSideToolkit.class);

        Runnable runnable = mock(Runnable.class);

        NotificationControl notificationControl = mock(NotificationControl.class);

        ServerInterfaceHandlerFactory<Runnable, Runnable> sihf = new RunnableHandlerFactory();

        try (RemoteConnection test = LocalRemoteConnection.with(new StandardArooaSession(), notificationControl)
                .setServerId("TEST-SERVER")
                .setExecutor(Runnable::run)
                .addHandlerFactory(sihf)
                .remoteForRoot(runnable)) {

            OperationType<Void> operationType = OperationType.ofName("run").returningVoid();

            test.invoke(1L, operationType);
        }

        verify(runnable, times(1)).run();
    }
}