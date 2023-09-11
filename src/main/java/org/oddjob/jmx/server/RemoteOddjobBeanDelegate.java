package org.oddjob.jmx.server;

import org.oddjob.arooa.registry.Address;
import org.oddjob.jmx.RemoteOddjobBean;

import java.util.Objects;

/**
 * The server side implementation of an {@link RemoteOddjobBean}.
 * <p>
 * Do we need this? It's created for every server side component and placed in the
 * {@Link ServerSideToolkit} so that it can be accessed by the
 * {@link org.oddjob.jmx.handlers.RemoteOddjobHandlerFactory} and delegated to. If the Handler this
 * created had access to the {@link org.oddjob.remote.Implementation}s of it's component it
 * could handle all invocations directly and not need to delegate.
 * </p>
 * <p>
 * Does having the ability to change this delegate bring flexibility?
 * </p>
 */
public class RemoteOddjobBeanDelegate implements RemoteOddjobBean {

    private final Address address;

    private ServerInterfaceManager implementationsProvider;

    public RemoteOddjobBeanDelegate(Address address) {
        this.address = address;
    }

    /**
     * Required to get round the chicken and egg situation of not initially knowing the
     * {@link ServerInterfaceManager} when this created but this is needed for the
     * {@link ServerSideToolkit} which is needed to created the {@link ServerInterfaceManager}!
     * Maybe a Supplier would be better?
     *
     * @param implementationsProvider The provider of implementations.
     */
    public void setImplementationsProvider(ServerInterfaceManager implementationsProvider) {
        this.implementationsProvider = Objects.requireNonNull(implementationsProvider);
    }

    /**
     * Get the component info.
     *
     * @return ServerInfo for the component.
     */
    public ServerInfo serverInfo() {

        return new ServerInfo(
                address,
                implementationsProvider.allClientInfo());
    }

    public void noop() {
    }
}
