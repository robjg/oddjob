/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.jmx.server;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaSession;

/**
 * A factory for producing OddjobMBeans. The MBean names are just sequential formatted numbers.
 * The root name is created with the 0 number.
 * 
 */
public class OddjobMBeanFactory implements ServerSession {
	private static final Logger logger = Logger.getLogger(OddjobMBeanFactory.class);
	
	/** The server */
	private final MBeanServer server;
	
	private final ArooaSession session;
	
	/** Give each bean a serial number. */
	private final AtomicInteger serial = new AtomicInteger();

	/** Keep track of MBeans by ObjectName so that we can destroy them 
	 * when they're finished. */
	private final Map<ObjectName, OddjobMBean> mBeans = 
		new HashMap<ObjectName, OddjobMBean>();
	
	private final Map<Object, ObjectName> names = 
		new HashMap<Object, ObjectName>();
	
	/**
	 * Constructor.
	 * 
	 * @param server The server to register newly created beans with.
	 */
	public OddjobMBeanFactory(MBeanServer server, ArooaSession session) {
		this.server = server;
		this.session = session;
	}
	
	/**
	 * Create an MBean and register with the server using the generated name.
	 * 
	 * @param object The object the MBean is wrapping.
	 * @return context The server context for the object.
	 * 
	 * @throws JMException If the MBean fails to register.
	 */
	public ObjectName createMBeanFor(Object object, ServerContext context)
	throws JMException {
		
		ObjectName objName = objectName(serial.getAndIncrement());
		
		OddjobMBean ojmb = new OddjobMBean(object, objName, this, context);
		
		server.registerMBean(ojmb, objName);
		
		synchronized (this) {
			names.put(object, objName);
			mBeans.put(objName, ojmb);
		}
		
		logger.debug("Created and registered [" + object + "] as OddjobMBean [" + objName.toString() + "]");
		return objName;
	}
	
	/**
	 * Remove a bean from the server.
	 * 
	 * @param objName The bean.
	 * @throws JMException
	 */
	public void destroy(ObjectName objName) throws JMException {

		OddjobMBean ojmb = null;
		
		synchronized (this) {
			ojmb = mBeans.remove(objName);
			names.remove(ojmb.getNode());
		}
		
		ojmb.destroy();
		
		server.unregisterMBean(objName);
		
		logger.debug("Unregistered and destroyed OddjobMBean [" + objName.toString() + "]");
	}
	
	/**
	 * Helper function to build the object name from the sequence number.
	 * 
	 * @param sequence The object sequence number.
	 * @return A JMX object name.
	 */
	public static ObjectName objectName(int sequence) {
		try {
			NumberFormat f = new DecimalFormat("00000000");
			String uid = f.format(sequence);
			return new ObjectName("oddjob", "uid", uid);
		}
		catch (MalformedObjectNameException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public ObjectName nameFor(Object object) {
		synchronized (this) {
			return names.get(object);
		}
	}
	
	public Object objectFor(ObjectName objectName) {
		OddjobMBean mBean = null;
		synchronized (this) {
			mBean = mBeans.get(objectName);
			if (mBean == null) {
				return null;
			}
		}
		return mBean.getNode();
	}
	
	@Override
	public ArooaSession getArooaSession() {
		return session;
	}
}
