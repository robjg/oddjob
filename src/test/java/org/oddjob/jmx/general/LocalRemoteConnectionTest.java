package org.oddjob.jmx.general;

import org.junit.jupiter.api.Test;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.handlers.RunnableHandlerFactory;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.remote.OperationType;
import org.oddjob.remote.RemoteConnection;
import org.oddjob.remote.RemoteException;
import org.oddjob.remote.util.NotificationControl;

import static org.mockito.Mockito.*;

class LocalRemoteConnectionTest {

    @Test
    void simpleRunnable() throws RemoteException {

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

            test.invoke(0L, operationType);
        }

        verify(runnable, times(1)).run();
    }
}