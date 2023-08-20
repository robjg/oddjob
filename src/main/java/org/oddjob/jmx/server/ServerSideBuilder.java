package org.oddjob.jmx.server;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.jmx.RemoteIdMappings;
import org.oddjob.remote.RemoteException;
import org.oddjob.util.SimpleThreadManager;
import org.oddjob.util.ThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Build a JMX Oddjob Bean Server.
 */
public class ServerSideBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ServerSideBuilder.class);

    private final ArooaSession session;

    private HandlerFactoryProvider handlerFactories;

    private Executor executor;

    private Map<String, ?> environment;

    private String logFormat;

    private  ServerSideBuilder(ArooaSession session) {
        this.session = session;
    }

    public static ServerSideBuilder withSession(ArooaSession session) {
        return new ServerSideBuilder(Objects.requireNonNull(session));
    }

    public ServerSideBuilder andHandlerFactories(HandlerFactoryProvider handlerFactories) {
        this.handlerFactories = handlerFactories;
        return this;
    }

    public ServerSideBuilder andExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public ServerSideBuilder andEnvironment(Map<String, ?> environment) {
        this.environment = environment;
        return this;
    }

    public ServerSideBuilder andLogFormat(String logFormat) {
        this.logFormat = logFormat;
        return this;
    }

    public ServerSide buildWith(MBeanServer mBeanServer, String serverId, Object root) throws RemoteException {
        return new Impl(this,
                Objects.requireNonNull(mBeanServer),
                Objects.requireNonNull(serverId),
                Objects.requireNonNull(root));
    }


    static class Impl implements ServerSide {

        /**
         * The ThreadManager. Handlers use this to avoid long running
         * connections.
         */
        private final ThreadManager threadManager;

        /** Bean Factory */
        private final OddjobMBeanFactory factory;

        private final MBeanServerConnection serverConnection;

        Impl(ServerSideBuilder builder, MBeanServer mBeanServer,
             String serverId, Object root) throws RemoteException {

            this.serverConnection = Objects.requireNonNull(mBeanServer);

            threadManager = new SimpleThreadManager(builder.executor);

            // Add supported interfaces.
            // note that some interfaces are hardwired in the factory because
            // they are aspects of the server.
            ServerInterfaceManagerFactoryImpl imf =
                    new ServerInterfaceManagerFactoryImpl(builder.environment);

            ServerInterfaceHandlerFactory<?, ?>[] sihfs =
                    new ResourceFactoryProvider(builder.session).getHandlerFactories();

            imf.addServerHandlerFactories(sihfs);

            if (builder.handlerFactories != null) {
                imf.addServerHandlerFactories(builder.handlerFactories.getHandlerFactories());
            }

            BeanDirectory registry = builder.session.getBeanRegistry();

            ServerModelImpl model = new ServerModelImpl(
                    new ServerId(serverId),
                    threadManager,
                    imf);

            model.setLogFormat(builder.logFormat);

            factory = new OddjobMBeanFactory(mBeanServer, builder.session);

            ServerMainBean serverBean = new ServerMainBean(
                    root,
                    registry);

            long mainName = factory.createMBeanFor(serverBean,
                    new ServerContextMain(model, serverBean));
            if (mainName != 0L) {
                throw new IllegalStateException("Main bean id should be 0 not " + mainName);
            }
        }

        @Override
        public RemoteIdMappings getRemoteIdMappings() {
            return factory;
        }

        @Override
        public MBeanServerConnection getServerConnection() {
            return serverConnection;
        }

        @Override
        public void close() {
            logger.debug("Stopping any running jobs.");
            threadManager.close();

            logger.debug("Destroying MBeans.");
            try {
                factory.destroy(0L);
            }
            catch (RemoteException e) {
                // This can happen when the RMI registry is shut before the
                // server is stopped.
                logger.error("Failed destroying main MBean.", e);
            }

        }
    }

}
