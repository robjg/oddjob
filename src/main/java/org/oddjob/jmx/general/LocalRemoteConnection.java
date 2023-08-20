package org.oddjob.jmx.general;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryOwner;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.utils.ClassUtils;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.server.*;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.monitor.context.AncestorContext;
import org.oddjob.remote.*;
import org.oddjob.remote.util.NotificationControl;
import org.oddjob.util.SimpleThreadManager;
import org.oddjob.util.ThreadManager;

import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Experimental implementation of an {@link RemoteConnection} that is local. It uses
 * {@link org.oddjob.jmx.server.ServerInterfaceHandlerFactory}s to provide the connection facade
 * in as close a way as JMX does.
 */
public class LocalRemoteConnection implements RemoteConnection {

    private final ServerInterfaceManagerFactory serverInterfaceManagerFactory;

    private final NotificationControl notificationControl;

    private final ServerSession serverSession;

    private final Runnable close;

    private final Map<Long, ServerInterfaceManager> nodes = new HashMap<>();

    private final AtomicLong nodeIds = new AtomicLong();

    private LocalRemoteConnection(ServerInterfaceManagerFactory serverInterfaceManagerFactory,
                                  ArooaSession arooaSession,
                                  NotificationControl notificationControl,
                                  Runnable close) {
        this.serverInterfaceManagerFactory = serverInterfaceManagerFactory;
        this.serverSession = new ServerSessionImpl(arooaSession);
        this.notificationControl = Objects.requireNonNull(notificationControl);
        this.close = close;
    }

    public static class Builder {

        private final ArooaSession arooaSession;

        private final NotificationControl notificationControl;

        private String serverId;

        private Executor executor;

        private String logFormat;


        private final ServerInterfaceManagerFactoryImpl.Builder factories =
                ServerInterfaceManagerFactoryImpl.newBuilder();

        public Builder(ArooaSession arooaSession,
                       NotificationControl notificationControl) {
            this.arooaSession = Objects.requireNonNull(arooaSession);
            this.notificationControl = Objects.requireNonNull(notificationControl);
        }

        public Builder addHandlerFactory(ServerInterfaceHandlerFactory<?, ?> factory) {
            factories.addHandlerFactory(factory);
            return this;
        }

        public Builder setServerId(String serverId) {
            this.serverId = serverId;
            return this;
        }

        public Builder setExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder setLogFormat(String logFormat) {
            this.logFormat = logFormat;
            return this;
        }

        public RemoteConnection remoteForRoot(Object root) {

            ServerInterfaceManagerFactory simf = factories.build();

            ThreadManager threadManager = new SimpleThreadManager(executor);

            ServerModelImpl model = new ServerModelImpl(
                    new ServerId(serverId),
                    threadManager,
                    simf);
            model.setLogFormat(logFormat);

            Runnable close = threadManager::close;

            LocalRemoteConnection remote = new LocalRemoteConnection(
                    simf, arooaSession, notificationControl, close);

            ServerContextRoot serverContextRoot = new ServerContextRoot(model, root);

            remote.create(root, serverContextRoot);

            return remote;
        }
    }

    public static Builder with(ArooaSession arooaSession,
                               NotificationControl notificationControl) {
        return new Builder(arooaSession, notificationControl);
    }

    protected long create(Object component, ServerContext serverContext) {

        long remoteId = nodeIds.getAndIncrement();

        Remote remoteBean = new Remote(serverContext.getAddress());

        ServerSideToolkit serverSideToolkit = ServerSideToolkitImpl
                .create(remoteId, notificationControl, serverSession, serverContext, remoteBean);

        ServerInterfaceManager sim = serverInterfaceManagerFactory.create(component, serverSideToolkit);

        // Chicken and egg situation with client info.
        remoteBean.implementationsProvider = sim;

        nodes.put(remoteId, sim);

        return remoteId;
    }

    @Override
    public void destroy(long remoteId) throws RemoteException {
        ServerInterfaceManager sim = nodes.get(remoteId);
        if (sim == null) {
            throw new RemoteIdException(remoteId, "No remote Id");
        }
        sim.destroy();
    }

    @Override
    public <T> T invoke(long remoteId, OperationType<T> operationType, Object... args) throws RemoteException {
        ServerInterfaceManager sim = nodes.get(remoteId);
        if (sim == null) {
            throw new RemoteIdException(remoteId, "No remote Id");
        }

        String[] paramTypes = ClassUtils.classesToStrings(operationType.getSignature());
        try {
            //noinspection unchecked
            return (T) sim.invoke(operationType.getName(), args, paramTypes);
        } catch (MBeanException | ReflectionException e) {
            throw new RemoteIdException(remoteId, "Failed executing " + operationType +
                    " with args " + Arrays.toString(args), e);
        }
    }

    @Override
    public <T> void addNotificationListener(long remoteId, NotificationType<T> notificationType, NotificationListener<T> notificationListener) throws RemoteException {

    }

    @Override
    public <T> void removeNotificationListener(long remoteId, NotificationType<T> notificationType, NotificationListener<T> notificationListener) throws RemoteException {

    }

    @Override
    public void close() throws RemoteException {
        close.run();
    }

    class ServerSessionImpl implements ServerSession {

        private final ArooaSession session;

        private final Map<Long, Object> componentsById = new HashMap<>();

        private final Map<Object, Long> idsByComponent = new HashMap<>();

        public ServerSessionImpl(ArooaSession session) {
            this.session = Objects.requireNonNull(session);
        }

        /**
         * Create an MBean and register with the server using the generated name.
         *
         * @param object The object the MBean is wrapping.
         * @return context The server context for the object.
         *
         * @throws RemoteException If the MBean fails to register.
         */
        @Override
        public long createMBeanFor(Object object, ServerContext context)
                throws RemoteException {

            long objectId = create(object, context);

            synchronized (this) {
                idsByComponent.put(object, objectId);
                componentsById.put(objectId, object);
            }

            return objectId;
        }

        /**
         * Remove a bean from the server.
         *
         * @param objectId The bean.
         * @throws RemoteException
         */
        @Override
        public void destroy(long objectId) throws RemoteException {

            LocalRemoteConnection.this.destroy(objectId);

        }

        @Override
        public long idFor(Object object) {
            synchronized (this) {
                return Optional.ofNullable(idsByComponent.get(object)).orElse(-1L);
            }
        }

        @Override
        public Object objectFor(long remoteId) {
            synchronized (this) {
                return componentsById.get(remoteId);
            }
        }

        @Override
        public ArooaSession getArooaSession() {
            return session;
        }
    }

    static class ServerContextRoot implements ServerContext {

        private final ServerModel model;

        private final Object component;

        /**
         * A constructor for the top most server
         * context.
         */
        public ServerContextRoot(
                ServerModel model,
                Object component) {

            this.model = model;
            this.component = component;
        }

        @Override
        public Object getThisComponent() {
            return component;
        }

        @Override
        public AncestorContext getParent() {
            return null;
        }

        @Override
        public ServerContext addChild(Object child) {
            return new ServerContextImpl(child, model, this);
        }

        @Override
        public ServerModel getModel() {
            return model;
        }

        @Override
        public LogArchiver getLogArchiver() {
            return null;
        }

        @Override
        public ConsoleArchiver getConsoleArchiver() {
            return null;
        }

        public ServerId getServerId() {
            return model.getServerId();
        }

        @Override
        public Address getAddress() {
            return null;
        }

        @Override
        public BeanDirectory getBeanDirectory() {

            if (component instanceof BeanDirectoryOwner) {
                return ((BeanDirectoryOwner) component).provideBeanDirectory();
            }
            else {
                return null;
            }
        }
    }

    static class Remote implements RemoteOddjobBean {

        private final Address address;

        private ServerInterfaceManager implementationsProvider;

        Remote(Address address) {
            this.address = address;
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

}
