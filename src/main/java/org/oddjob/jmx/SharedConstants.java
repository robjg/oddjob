/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.Describable;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.DirectInvocationClientFactory;
import org.oddjob.jmx.handlers.*;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;

/**
 * Constants shared between client and server.
 *
 * @author Rob Gordon.
 */
public class SharedConstants {

    /**
     * The retrieve log events method name.
     */
    public static final String RETRIEVE_LOG_EVENTS_METHOD = "retrieveLogEvents";

    /**
     * The retrieve console events method name.
     */
    public static final String RETRIEVE_CONSOLE_EVENTS_METHOD = "retrieveConsoleEvents";

    /**
     * The to string method name.
     */
    public static final String TO_STRING_METHOD = "toString";

    /**
     * Get logger method name.
     */
    public static final String GET_LOGGER_METHOD = "loggerName";


    public static final ServerInterfaceHandlerFactory<?, ?>[] DEFAULT_SERVER_HANDLER_FACTORIES
            = {
            new DynaBeanHandlerFactory(),
            new ExportableHandlerFactory(),
            new LogEnabledHandlerFactory(),
            new LogPollableHandlerFactory(),
            new ObjectInterfaceHandlerFactory(),
            new DescribableHandlerFactory(),
            new RemoteOddjobHandlerFactory(),
    };

    public static final ClientInterfaceHandlerFactory<?>[] DEFAULT_CLIENT_HANDLER_FACTORIES
            = {
            new DirectInvocationClientFactory<>(DynaBean.class),
            new ExportableHandlerFactory.ClientFactory(),
            new LogEnabledHandlerFactory.ClientFactory(),
            new LogPollableHandlerFactory.ClientFactory(),
            new ObjectInterfaceHandlerFactory.ClientFactory(),
            new DirectInvocationClientFactory<>(Describable.class),
            new DirectInvocationClientFactory<>(RemoteOddjobBean.class),
    };
}
