package org.oddjob.jmx.server;

import java.io.NotSerializableException;
import java.rmi.RemoteException;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.Utils;

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
 * 
 * @author Rob Gordon.
 */

public class OddjobMBean extends NotificationBroadcasterSupport implements
		DynamicMBean {
	private static final Logger logger = LoggerFactory.getLogger(OddjobMBean.class);

	/** The server node this object represents. */
	private final Object node;

	/** The server context for this OddjobMBean. */
	private final ServerContext srvcon;
	
	/** The notification sequenceNumber */
	private int sequenceNumber = 0;

	private final ObjectName objectName;
	
	/** The factory for adding and removing beans. */
	private final ServerSession factory;

	/** The interface manager. */
	private final ServerInterfaceManager serverInterfaceManager;
		
	/** Used to ensure that no fresh notificatons are sent during a resync. */
	private final Object resyncLock = new Object();

	/**
	 * Constructor.
	 * 
	 * @param node The job this is shadowing.
	 * @param objectName The objectName for this node.
	 * @param factory The factory for creating child OddjobMBeans. May be null only
	 * if this MBean will never have children.
	 * @param srvcon The server context The server context. Must not be null.
	 * 
	 * @throws RemoteException
	 */
	public OddjobMBean(Object node, ObjectName objectName,
			ServerSession factory, ServerContext srvcon) {
		
		if (node == null) {
			throw new NullPointerException("Component must not be null");
		}
		if (objectName == null) {
			throw new NullPointerException("Object Name must not be null");
		}
		if (srvcon == null) {
			throw new NullPointerException("Server Context must not be null");
		}
		
		this.node = node;
		this.factory = factory;
		this.srvcon = srvcon;
		this.objectName = objectName;
		
		ServerInterfaceManagerFactory imf = 
			srvcon.getModel().getInterfaceManagerFactory();
		
		serverInterfaceManager = imf.create(node, new Toolkit());		
	}

	public Object getNode() {
		return node;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attribute)
	throws ReflectionException, MBeanException {
		logger.debug("getAttribute(" + attribute + ")");
		return invoke("get", new Object[] { attribute },
				new String[] { String.class.getName() });
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
	 */
	public void setAttribute(Attribute attribute) 
	throws ReflectionException, MBeanException {
		logger.debug("setAttribute(" + attribute.getName() + ")");
		invoke("set", new Object[] { attribute.getClass(), attribute.getValue() },
				new String[] { String.class.getName(), Object.class.getName() });
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
	 */
	public AttributeList getAttributes(String[] attributes) {
		AttributeList al = new AttributeList();
		for (int i = 0; i < attributes.length; ++i) {
			String attribute = attributes[i];
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

	/**
	 * Utility function to build a method name from the invoke method arguments.
	 * 
	 * @return A String method description.
	 */
	static String methodDescription(final String actionName, String[] signature) {
		// build an incredibly converluted debug message.
		StringBuffer buf = new StringBuffer();
		buf.append(actionName);
		buf.append('(');
		for (int j = 0; j < signature.length; ++j) {
			buf.append(j== 0 ? "" : ", ");
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
		if (logger.isDebugEnabled()) {
			String methodDescription = methodDescription(actionName, signature);
			logger.debug("Invoking [" + 
					methodDescription + "] on [" + node + "]");
		}
		
		////////////////////////////////////////////////////////////
		
		// anything else - pass to the interface manager.
		Object[] imported = Utils.importResolve(params, factory);
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

	class Remote implements RemoteOddjobBean {
		
		/**
		 * Get the component info.
		 * 
		 * @return ServerInfo for the component.
		 */
		public ServerInfo serverInfo() {
			return new ServerInfo(
					srvcon.getAddress(), 
					serverInterfaceManager.allClientInfo());
		}
		
		public void noop() {
		}
	}
		
	class Toolkit implements ServerSideToolkit {
	
		
		public void sendNotification(Notification notification) {
			OddjobMBean.this.sendNotification(notification);
		}
		
		public Notification createNotification(String type) {
			synchronized(resyncLock) {
				return new Notification(type, objectName, sequenceNumber++);
			}
		}
		
		/**
		 * Used by handlers to execute functionallity while
		 * holding the resync lock.
		 * 
		 * @param runnable The functionality to run.
		 */
		public void runSynchronized(Runnable runnable) {
			synchronized (resyncLock) {
				runnable.run();
			}		
		}
						
		/**
		 * Gives handlers access to the server context.
		 * 
		 * @return The server context for this MBean.
		 */
		public ServerContext getContext() {
			return srvcon;
		}
		
		public RemoteOddjobBean getRemoteBean() {
			return new Remote();
		}
		
		public ServerSession getServerSession() {
			return factory;
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " for [" + node + 
				"], name " + objectName;
	}
}
