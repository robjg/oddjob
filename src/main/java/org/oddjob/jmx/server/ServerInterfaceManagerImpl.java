/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.arooa.utils.Pair;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.general.RemoteBridge;
import org.oddjob.remote.*;

import javax.management.*;
import java.util.*;

/**
 * Simple Implementation of an InterfaceManager.
 *
 * @author Rob Gordon
 */
public class ServerInterfaceManagerImpl implements ServerInterfaceManager {

    private final long remoteId;

    /**
     * The collective mBeanInfo
     */
    private final MBeanInfo mBeanInfo;

    private final Set<NotificationType<?>> notificationTypes;

    /**
     * Remember the handler info needed later
     */
    private final Pair<ServerInterfaceHandler, ServerInterfaceHandlerFactory<?, ?>>[] handlerAndFactory;

    /**
     * Map of methods to InterfaceHandlers. Not sure if the order
     * interface might be important but we are using a LinkedHashMap just
     * in case it is.
     */
    private final Map<RemoteOperation<?>, ServerInterfaceHandler> operations;

    /**
     * Map of remote operations.
     */
    private final Map<RemoteOperation<?>, MBeanOperationInfo> opInfos;

    /**
     * Simple Security
     */
    private final OddjobJMXAccessController accessController;

    public ServerInterfaceManagerImpl(Object target,
                                      ServerSideToolkit ojmb,
                                      ServerInterfaceHandlerFactory<?, ?>[] serverHandlerFactories) {
        this(target, ojmb, serverHandlerFactories, null);
    }

    /**
     * Constructor.
     *
     * @param target  The target object the OddjobMBean is representing.
     * @param toolkit The Server Side Toolkit.
     * @param serverHandlerFactories The Interface Handler Factories.
     * @param accessController An Access Controller.
     */
    public ServerInterfaceManagerImpl(Object target,
                                      ServerSideToolkit toolkit,
                                      ServerInterfaceHandlerFactory<?, ?>[] serverHandlerFactories,
                                      OddjobJMXAccessController accessController) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(toolkit);
        Objects.requireNonNull(serverHandlerFactories);

        this.remoteId = toolkit.getRemoteId();
        this.operations = new LinkedHashMap<>();
        this.opInfos = new HashMap<>();

        List<MBeanAttributeInfo> attributeInfo =
                new ArrayList<>();
        List<MBeanOperationInfo> operationInfo =
                new ArrayList<>();
        List<NotificationType<?>> notificationInfo =
                new ArrayList<>();

        List<Pair<ServerInterfaceHandler, ServerInterfaceHandlerFactory<?, ?>>> handlerAndFactory =
                new ArrayList<>();

        // Loop over all definitions.
        for (ServerInterfaceHandlerFactory<?, ?> serverHandlerFactory : serverHandlerFactories) {

            // create the interface handler
            ServerInterfaceHandler interfaceHandler
                    = create(target, toolkit, serverHandlerFactory);

            if (interfaceHandler == null) {
                continue;
            }

            handlerAndFactory.add(Pair.of(interfaceHandler,
                    serverHandlerFactory));

            // collate MBeanAttributeInfo.
            attributeInfo.addAll(Arrays.asList(serverHandlerFactory.getMBeanAttributeInfo()));

            // collate MBeanOperationInfo.
            MBeanOperationInfo[] oInfo = serverHandlerFactory.getMBeanOperationInfo();


            for (MBeanOperationInfo opInfo : oInfo) {
                operationInfo.add(opInfo);
                RemoteOperation<?> remoteOp =
                        new OperationInfoOperation(opInfo);
                operations.put(
                        remoteOp,
                        interfaceHandler);
                opInfos.put(remoteOp, opInfo);
            }

            // collate MBeanNotificationInfo.
            notificationInfo.addAll(serverHandlerFactory.getNotificationTypes());
        }

        MBeanNotificationInfo[] notificationInfos = notificationInfo.stream()
                .map(RemoteBridge::toMBeanNotification)
                .toArray(MBeanNotificationInfo[]::new);

        this.notificationTypes = new HashSet<>(notificationInfo);

        //noinspection unchecked
        this.handlerAndFactory = handlerAndFactory.toArray(new Pair[0]);

        // create an MBeanInfo from the collated information.
        mBeanInfo = new MBeanInfo(target.toString(),
                "Description of " + target,
                attributeInfo.toArray(new MBeanAttributeInfo[0]),
                new MBeanConstructorInfo[0],
                operationInfo.toArray(new MBeanOperationInfo[0]),
                notificationInfos);

        this.accessController = Objects.requireNonNullElseGet(accessController, () -> opInfo -> true);
    }

    private ServerInterfaceManagerImpl(long remoteId,
                                       MBeanInfo mBeanInfo,
                                       Set<NotificationType<?>> notificationTypes,
                                       Pair<ServerInterfaceHandler, ServerInterfaceHandlerFactory<?, ?>>[] handlerAndFactory,
                                       Map<RemoteOperation<?>, ServerInterfaceHandler> operations,
                                       Map<RemoteOperation<?>, MBeanOperationInfo> opInfos,
                                       OddjobJMXAccessController accessController) {
        this.remoteId = remoteId;
        this.mBeanInfo = mBeanInfo;
        this.notificationTypes = notificationTypes;
        this.handlerAndFactory = handlerAndFactory;
        this.operations = operations;
        this.opInfos = opInfos;
        this.accessController = accessController;
    }

    public static ServerInterfaceManager createFor(Object target,
                                      ServerSideToolkit toolkit,
                                      ServerInterfaceHandlerFactory<?, ?>[] serverHandlerFactories,
                                      OddjobJMXAccessController accessController) {

        List<MBeanAttributeInfo> attributeInfo =
                new ArrayList<>();
        List<MBeanOperationInfo> operationInfo =
                new ArrayList<>();
        List<NotificationType<?>> notificationInfo =
                new ArrayList<>();

        Map<RemoteOperation<?>, ServerInterfaceHandler> operations =
                new LinkedHashMap<>();

        Map<RemoteOperation<?>, MBeanOperationInfo> opInfos =
                new HashMap<>();

        List<Pair<ServerInterfaceHandler, ServerInterfaceHandlerFactory<?, ?>>> handlerAndFactory =
                new ArrayList<>();

        // Loop over all definitions.
        for (ServerInterfaceHandlerFactory<?, ?> serverHandlerFactory : serverHandlerFactories) {

            // create the interface handler
            ServerInterfaceHandler interfaceHandler
                    = create(target, toolkit, serverHandlerFactory);

            if (interfaceHandler == null) {
                continue;
            }

            handlerAndFactory.add(Pair.of(interfaceHandler,
                    serverHandlerFactory));

            // collate MBeanAttributeInfo.
            attributeInfo.addAll(Arrays.asList(serverHandlerFactory.getMBeanAttributeInfo()));

            // collate MBeanOperationInfo.
            MBeanOperationInfo[] oInfo = serverHandlerFactory.getMBeanOperationInfo();


            for (MBeanOperationInfo opInfo : oInfo) {
                operationInfo.add(opInfo);
                RemoteOperation<?> remoteOp =
                        new OperationInfoOperation(opInfo);
                operations.put(
                        remoteOp,
                        interfaceHandler);
                opInfos.put(remoteOp, opInfo);
            }

            // collate MBeanNotificationInfo.
            notificationInfo.addAll(serverHandlerFactory.getNotificationTypes());
        }

        MBeanNotificationInfo[] notificationInfos = notificationInfo.stream()
                .map(RemoteBridge::toMBeanNotification)
                .toArray(MBeanNotificationInfo[]::new);

        Set<NotificationType<?>> notificationTypes = new HashSet<>(notificationInfo);

        //noinspection unchecked
        Pair<ServerInterfaceHandler, ServerInterfaceHandlerFactory<?, ?>>[] handlersAndFactories
                = handlerAndFactory.toArray(new Pair[0]);

        // create an MBeanInfo from the collated information.
        MBeanInfo mBeanInfo = new MBeanInfo(target.toString(),
                "Description of " + target,
                attributeInfo.toArray(new MBeanAttributeInfo[0]),
                new MBeanConstructorInfo[0],
                operationInfo.toArray(new MBeanOperationInfo[0]),
                notificationInfos);

        if (accessController == null) {
            accessController = opInfo -> true;
        }

        return new ServerInterfaceManagerImpl(toolkit.getRemoteId(),
                mBeanInfo,
                notificationTypes,
                handlersAndFactories,
                operations,
                opInfos,
                accessController);
    }

    private static <S> ServerInterfaceHandler create(
            Object target,
            ServerSideToolkit ojmb,
            ServerInterfaceHandlerFactory<S, ?> factory) {

        Class<S> type = factory.serverClass();

        if (!type.isInstance(target)) {
            throw new ClassCastException("" + target +
                    " not of type " + type.getName());
        }

        // create the interface handler

        return factory.createServerHandler(
                type.cast(target), ojmb);
    }

    @Override
    public Implementation<?>[] allClientInfo() {

        List<Implementation<?>> implementations =
                new ArrayList<>();

        handler:
        for (Pair<ServerInterfaceHandler,
                ServerInterfaceHandlerFactory<?, ?>> pair : handlerAndFactory) {

            ServerInterfaceHandlerFactory<?, ?> factory = pair.getRight();

            for (MBeanOperationInfo opInfo : factory.getMBeanOperationInfo()) {
                if (!accessController.isAccessible(opInfo)) {
                    continue handler;
                }
            }

            ServerInterfaceHandler handler = pair.getLeft();

            String clientClassName = factory.clientClass().getName();
            String handlerVersion = factory.getHandlerVersion().getVersionAsText();

            if (handler instanceof HasInitialisation) {
                implementations.add(Implementation.create(
                        clientClassName,
                        handlerVersion,
                        ((HasInitialisation<?>) handler).initialisation()));
            }
            else {
                implementations.add(Implementation.create(
                        clientClassName,
                        handlerVersion));
			}
        }

        return implementations.toArray(new Implementation[0]);
    }

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jmx.server.InterfaceManager#getMBeanInfo()
     */
    public MBeanInfo getMBeanInfo() {
        return mBeanInfo;
    }

    @Override
    public Set<NotificationType<?>> getNotificationTypes() {
        return notificationTypes;
    }

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jmx.server.InterfaceManager#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
     */
    @Override
    public Object invoke(String actionName,
                         Object[] params, String[] signature)
            throws RemoteException {
        RemoteOperation<Object> op = new MBeanOperation(actionName, signature);

        ServerInterfaceHandler interfaceHandler = operations.get(op);
        if (interfaceHandler == null) {
            throw new IllegalArgumentException(
                    "No interface supports method [" + op + "], operations are " +
                            operations.keySet());
        }
        MBeanOperationInfo opInfo = this.opInfos.get(op);
        if (opInfo == null) {
            throw new RuntimeException(
                    "No OpInfo for [" + op + "] (This is a bug!)");
        }
        if (!accessController.isAccessible(opInfo)) {
            throw new SecurityException(
                    "Access denied! Invalid access level for " + op);
        }

        try {
            return interfaceHandler.invoke(op, params);
        } catch (RemoteException e) {
            throw e;
        } catch (Throwable e) {
            throw RemoteInvocationException.of(remoteId,
                    actionName, signature, params, e);
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jmx.server.InterfaceManager#destroy()
     */
    public void destroy() {
        for (Pair<ServerInterfaceHandler,
                ServerInterfaceHandlerFactory<?, ?>> pair : handlerAndFactory) {
            pair.getLeft().destroy();
        }
    }
}