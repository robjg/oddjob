package org.oddjob.jmx.server;

import org.oddjob.arooa.registry.Address;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.NotSerializableException;
import java.util.Objects;

/**
 * A MBean which wraps an object providing an Oddjob management interface to the
 * object.
 * <p>
 * Once the bean is created it will sit and wait for clients to interrogate it. When
 * a client accesses the bean it should call the resync method which will cause the
 * bean to resend the notifications necessary to recreate in the client, the state
 * of the bean. During the resync the InterfaceHandlers should block any any more
 * changes until the resync has completed.
 *
 * @author Rob Gordon.
 */

public class OddjobMBean implements
        NotificationEmitter, DynamicMBean {
    private static final Logger logger = LoggerFactory.getLogger(OddjobMBean.class);

    /**
     * The server node this object represents.
     */
    private final Object node;

    private final ObjectName objectName;

    /**
     * The factory for adding and removing beans.
     */
    private final ServerSession serverSession;

    /**
     * The interface manager.
     */
    private final ServerInterfaceManager serverInterfaceManager;

    private final NotificationEmitter listeners;

    /**
     * Constructor.
     *
     * @param node                   The job this is shadowing.
     * @param objectName             The object name for this node.
     * @param serverSession          The factory for creating child OddjobMBeans. May be null only
     *                               if this MBean will never have children.
     * @param serverInterfaceManager The Server Interface Manager responsible for invoking operations. Must not be null.
     * @param notificationEmitter    The Notification Emitter responsible for registering and removing listeners. Must
     *                               not be null.
     */
    private OddjobMBean(Object node,
                        ObjectName objectName,
                        ServerSession serverSession,
                        ServerInterfaceManager serverInterfaceManager,
                        NotificationEmitter notificationEmitter) {

        this.node = Objects.requireNonNull(node, "Component must not be null");
        this.objectName = Objects.requireNonNull(objectName);
        this.serverSession = Objects.requireNonNull(serverSession);
        this.serverInterfaceManager = Objects.requireNonNull(serverInterfaceManager);
        this.listeners = Objects.requireNonNull(notificationEmitter);
    }

    /**
     * Constructor.
     *
     * @param node          The job this is shadowing.
     * @param remoteId      The remote id for this node.
     * @param serverSession The factory for creating child OddjobMBeans. May be null only
     *                      if this MBean will never have children.
     * @param serverContext The server context The server context. Must not be null.
     * @return A fully initialised OddjobMBean.
     */
    public static OddjobMBean create(Object node,
                                     long remoteId,
                                     ServerSession serverSession,
                                     ServerContext serverContext) {

        ServerInterfaceManagerFactory imf =
                serverContext.getModel().getInterfaceManagerFactory();

        Remote remoteBean = new Remote(serverContext.getAddress());

        ObjectName objectName = OddjobMBeanFactory.objectName(remoteId);

        JmxListenerHelper listeners = new JmxListenerHelper(objectName);

        ServerInterfaceManager serverInterfaceManager = imf.create(node,
                ServerSideToolkitImpl.create(remoteId, listeners, serverSession, serverContext,
                        remoteBean));

        // Chicken and egg situation with client info.
        remoteBean.implementationsProvider = serverInterfaceManager;

        listeners.setNotificationTypes(serverInterfaceManager.getNotificationTypes());

        return new OddjobMBean(node, objectName, serverSession, serverInterfaceManager, listeners);
    }

    public static OddjobMBean create(Object node,
                                     long remoteId,
                                     ServerSession serverSession,
                                     ServerInterfaceManager serverInterfaceManager,
                                     NotificationEmitter notificationEmitter) {

        return new OddjobMBean(node,
                OddjobMBeanFactory.objectName(remoteId),
                serverSession, serverInterfaceManager, notificationEmitter);
    }


    public Object getNode() {
        return node;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    /*
     *  (non-Javadoc)
     * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String attribute)
            throws ReflectionException, MBeanException {
        logger.debug("getAttribute(" + attribute + ")");
        return invoke("get", new Object[]{attribute},
                new String[]{String.class.getName()});
    }

    /*
     *  (non-Javadoc)
     * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
     */
    @Override
    public void setAttribute(Attribute attribute)
            throws ReflectionException, MBeanException {
        logger.debug("setAttribute(" + attribute.getName() + ")");
        invoke("set", new Object[]{attribute.getClass(), attribute.getValue()},
                new String[]{String.class.getName(), Object.class.getName()});
    }

    /*
     *  (non-Javadoc)
     * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
     */
    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList al = new AttributeList();
        for (String attribute : attributes) {
            Attribute attr;
            try {
                attr = new Attribute(attribute, getAttribute(attribute));
                al.add(attr);
            } catch (ReflectionException | MBeanException e) {
                logger.debug("Get attributes.", e);
            }
        }
        return al;
    }

    /*
     *  (non-Javadoc)
     * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
     */
    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList al = new AttributeList();
        for (Attribute attribute : attributes.asList()) {
            try {
                setAttribute(attribute);
                al.add(attribute);
            } catch (ReflectionException | MBeanException e) {
                logger.debug("Set attributes.", e);
            }
        }
        return al;
    }

    @Override
    public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException {
        this.listeners.removeNotificationListener(listener, filter, handback);
    }

    @Override
    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException {
        this.listeners.addNotificationListener(listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        this.listeners.removeNotificationListener(listener);
    }

    /**
     * Utility function to build a method name from the invoke method arguments.
     *
     * @return A String method description.
     */
    static String methodDescription(final String actionName, String[] signature) {
        // build an incredibly convoluted debug message.
        StringBuilder buf = new StringBuilder();
        buf.append(actionName);
        buf.append('(');
        for (int j = 0; j < signature.length; ++j) {
            buf.append(j == 0 ? "" : ", ");
            buf.append(signature[j]);
        }
        buf.append(")");
        return buf.toString();
    }

    /*
     *  (non-Javadoc)
     * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object invoke(final String actionName, final Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        if (logger.isTraceEnabled()) {
            String methodDescription = methodDescription(actionName, signature);
            logger.trace("Invoking [{}] on [{}]", methodDescription, node);
        }

        ////////////////////////////////////////////////////////////

        // anything else - pass to the interface manager.
        Object[] imported = Utils.importResolve(params, serverSession);
        if (imported == null) {
            // ensure null params is converted to 0 length array.
            imported = new Object[0];
        }
        Object result = serverInterfaceManager.invoke(actionName, imported, signature);
        try {
            return Utils.export(result);
        } catch (NotSerializableException e) {
            throw new MBeanException(e, "Failed to return result from [" + actionName + "(...)]");
        }
    }

    /*
     *  (non-Javadoc)
     * @see javax.management.DynamicMBean#getMBeanInfo()
     */
    public MBeanInfo getMBeanInfo() {
        return serverInterfaceManager.getMBeanInfo();
    }

    /*
     *  (non-Javadoc)
     * @see javax.management.NotificationBroadcaster#getNotificationInfo()
     */
    public MBeanNotificationInfo[] getNotificationInfo() {
        return serverInterfaceManager.getMBeanInfo().getNotifications();
    }

    /**
     * Destroy this node. Notify all remote listeners their peer is dead.
     */
    public void destroy() {
        logger.debug("Destroying [" + this + "]");
        serverInterfaceManager.destroy();
    }

    @Override
    public String toString() {
        return "OddjobMBean for [" + node +
                "], name " + objectName;
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
