package org.oddjob.jmx.handlers;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.jmx.client.*;
import org.oddjob.jmx.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provide a Mock for testing remoting between client and serer without using RMI.
 *
 * @param <S>
 * @param <T>
 */
public class MockRemote<S, T> implements ArooaSessionAware, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MockRemote.class);

    static final long REMOTE_ID = 50L;

    private ServerInterfaceHandlerFactory<S, T> serverFactory;

    private ClientInterfaceHandlerFactory<T> clientFactory;

    private ArooaSession arooaSession;

    private Object parent;

    private S serverObject;

    private T clientObject;

    public void run() {

        try {
            doRun();
        } catch (NotCompliantMBeanException | InstanceAlreadyExistsException | MBeanRegistrationException e) {
            throw new RuntimeException(e);
        }
    }

    void doRun() throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        assertThat(serverFactory.getHandlerVersion(), sameInstance(clientFactory.getVersion()));
        assertThat(serverFactory.clientClass(), sameInstance(clientFactory.interfaceClass()));

        S serverObject = Objects.requireNonNull(this.serverObject);

        assertThat(serverObject, instanceOf(serverFactory.serverClass()));

        // Server Side

        ServerInterfaceManagerFactory simf = new ServerInterfaceManagerFactoryImpl(
                new ServerInterfaceHandlerFactory[]{
                        new RemoteOddjobHandlerFactory(),
                        new ObjectInterfaceHandlerFactory(),
                        serverFactory});

        ServerModel serverModel = mock(ServerModel.class);
        when(serverModel.getInterfaceManagerFactory())
                .thenReturn(simf);

        ServerContext parentContext;
        if (parent == null) {
            parentContext = null;
        } else {
            parentContext = mock(ServerContext.class);
            when(parentContext.getParent()).thenReturn(null);
            when(parentContext.getThisComponent()).thenReturn(parent);
            when(parentContext.findAncestorOfType(any())).thenCallRealMethod();
        }

        ServerContext serverContext = mock(ServerContext.class);
        when(serverContext.getParent()).thenReturn(parentContext);
        when(serverContext.getThisComponent()).thenReturn(serverObject);
        when(serverContext.findAncestorOfType(any())).thenCallRealMethod();
        when(serverContext.getModel()).thenReturn(serverModel);

        // Server Bean

        MBeanServer mbs = MBeanServerFactory.createMBeanServer();

        OddjobMBean oddjobMBean = new OddjobMBean(serverObject, REMOTE_ID, null, serverContext);
        ObjectName objectName = oddjobMBean.getObjectName();

        mbs.registerMBean(oddjobMBean, objectName);

        // Client


        ClientInterfaceManagerFactory cimf = new ClientInterfaceManagerFactoryBuilder()
                .addFactories(new ObjectInterfaceHandlerFactory.ClientFactory(),
                        clientFactory)
                .build();

        ClientSessionImpl clientSession = new ClientSessionImpl(
                mbs,
                new DummyNotificationProcessor(),
                cimf,
                arooaSession,
                logger);

        //noinspection unchecked
        clientObject = (T) clientSession.create(REMOTE_ID);

        assertThat(clientObject, instanceOf(serverFactory.clientClass()));
    }

    public ServerInterfaceHandlerFactory<S, T> getServerFactory() {
        return serverFactory;
    }

    public void setServerFactory(ServerInterfaceHandlerFactory<S, T> serverFactory) {
        this.serverFactory = serverFactory;
    }

    public ClientInterfaceHandlerFactory<T> getClientFactory() {
        return clientFactory;
    }

    public void setClientFactory(ClientInterfaceHandlerFactory<T> clientFactory) {
        this.clientFactory = clientFactory;
    }


    public ArooaSession getArooaSession() {
        return arooaSession;
    }

    @Override
    public void setArooaSession(ArooaSession arooaSession) {
        this.arooaSession = arooaSession;
    }

    public T getClientObject() {
        return clientObject;
    }

    public S getServerObject() {
        return serverObject;
    }

    @ArooaAttribute
    public void setServerObject(S serverObject) {
        this.serverObject = serverObject;
    }

    @ArooaAttribute
    public void setParent(Object parent) {
        this.parent = parent;
    }
}
