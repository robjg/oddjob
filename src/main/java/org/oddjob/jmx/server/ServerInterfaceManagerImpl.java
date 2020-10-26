/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.arooa.utils.Pair;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.remote.HasInitialisation;
import org.oddjob.remote.Implementation;

import javax.management.*;
import java.util.*;

/**
 * Simple Implementation of an InterfaceManager.
 *
 * @author Rob Gordon
 */
public class ServerInterfaceManagerImpl implements ServerInterfaceManager {

    /**
     * The collective mBeanInfo
     */
    private final MBeanInfo mBeanInfo;

    /**
     * Remember the handler info needed later
     */
    private final Pair<ServerInterfaceHandler,
            ServerInterfaceHandlerFactory<?, ?>>[] handlerAndFactory;

    /**
     * Map of methods to InterfaceHandlers. Not sure if the order
     * interface might be important but we are using a LinkedHashMap just
     * in case it is.
     */
    private final Map<RemoteOperation<?>, ServerInterfaceHandler> operations =
            new LinkedHashMap<>();

    /**
     * Map of remote operations.
     */
    private final Map<RemoteOperation<?>, MBeanOperationInfo> opInfos =
            new HashMap<>();

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
     * @param target                 The target object the OddjobMBean is representing.
     * @param ojmb                   The OddjobMBean.
     * @param serverHandlerFactories The InterfaceInfos.
     */
    public ServerInterfaceManagerImpl(Object target,
                                      ServerSideToolkit ojmb,
                                      ServerInterfaceHandlerFactory<?, ?>[] serverHandlerFactories,
                                      OddjobJMXAccessController accessController) {

        List<MBeanAttributeInfo> attributeInfo =
                new ArrayList<>();
        List<MBeanOperationInfo> operationInfo =
                new ArrayList<>();
        List<MBeanNotificationInfo> notificationInfo =
                new ArrayList<>();

        handlerAndFactory = new Pair[serverHandlerFactories.length];

        // Loop over all definitions.
        for (int i = 0; i < serverHandlerFactories.length; ++i) {
            ServerInterfaceHandlerFactory<?, ?> serverHandlerFactory = serverHandlerFactories[i];

            // create the interface handler
            ServerInterfaceHandler interfaceHandler
                    = create(target, ojmb, serverHandlerFactory);

            handlerAndFactory[i] = Pair.of(interfaceHandler,
                    serverHandlerFactory);

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
            notificationInfo.addAll(Arrays.asList(serverHandlerFactory.getMBeanNotificationInfo()));
        }

        // create an MBeanInfo from the collated informations.
        mBeanInfo = new MBeanInfo(target.toString(),
                "Description of " + target.toString(),
                attributeInfo.toArray(new MBeanAttributeInfo[0]),
                new MBeanConstructorInfo[0],
                operationInfo.toArray(new MBeanOperationInfo[0]),
                notificationInfo.toArray(new MBeanNotificationInfo[0]));

        if (accessController == null) {
            this.accessController = opInfo -> true;
        } else {
            this.accessController = accessController;
        }
    }

    private <S> ServerInterfaceHandler create(
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
                        ((HasInitialisation) handler).initialisation()));
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

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jmx.server.InterfaceManager#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
     */
    @Override
    public Object invoke(String actionName,
                         Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
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
        return interfaceHandler.invoke(op, params);
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