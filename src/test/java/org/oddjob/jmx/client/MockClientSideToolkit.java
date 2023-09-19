package org.oddjob.jmx.client;

import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceManager;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.RemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MockClientSideToolkit implements ClientSideToolkit {

    private static final Logger logger = LoggerFactory.getLogger(MockClientSideToolkit.class);

    @Override
    public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args) throws RemoteException {
        throw new RuntimeException("Unexpected from " + getClass());
    }

    @Override
    public ClientSession getClientSession() {
        throw new RuntimeException("Unexpected from " + getClass());
    }

    @Override
    public <T> void registerNotificationListener(NotificationType<T> eventType,
                                                 NotificationListener<T> notificationListener) {
        throw new RuntimeException("Unexpected from " + getClass());
    }

    @Override
    public <T> void removeNotificationListener(NotificationType<T> eventType,
                                               NotificationListener<T> notificationListener) {
        throw new RuntimeException("Unexpected from " + getClass());
    }

    public static ClientSideToolkit mockToolkit(ServerInterfaceManager serverInterfaceManager) throws RemoteException {

        ClientSideToolkit toolkit = mock(ClientSideToolkit.class);
        Mockito.doAnswer(invocation -> {
                    Object[] rawArgs = invocation.getRawArguments();
                    RemoteOperation<?> remoteOperation = (RemoteOperation<?>) rawArgs[0];
                    Object[] args = (Object[]) rawArgs[1];
                    logger.debug("Invoking {} with args {}", remoteOperation, args);
                    return serverInterfaceManager.invoke(
                            remoteOperation.getActionName(),
                            args,
                            remoteOperation.getSignature());
                })
                .when(toolkit)
                .invoke(any(RemoteOperation.class),
                        AdditionalMatchers.or(ArgumentMatchers.isNull(Object[].class), any(Object[].class)));
        return toolkit;
    }


    public static ClientSideToolkit mockToolkit(ServerInterfaceHandler serverInterfaceHandler) throws RemoteException {

        ClientSideToolkit toolkit = mock(ClientSideToolkit.class);
        Mockito.doAnswer(invocation -> {
                    Object[] rawArgs = invocation.getRawArguments();
                    RemoteOperation<?> remoteOperation = (RemoteOperation<?>) rawArgs[0];
                    Object[] args = (Object[]) rawArgs[1];
                    logger.debug("Invoking {} with args {}", remoteOperation, args);
                    return serverInterfaceHandler.invoke(remoteOperation, args);
                })
                .when(toolkit)
                .invoke(any(RemoteOperation.class),
                        AdditionalMatchers.or(ArgumentMatchers.isNull(Object[].class), any(Object[].class)));
        return toolkit;
    }

    public static ClientSideToolkit mockToolkit(ServerInterfaceHandler serverInterfaceHandler,
                                                ClientSession clientSession) throws RemoteException {

        ClientSideToolkit toolkit = mockToolkit(serverInterfaceHandler);
        when(toolkit.getClientSession()).thenReturn(clientSession);
        return toolkit;
    }

}
