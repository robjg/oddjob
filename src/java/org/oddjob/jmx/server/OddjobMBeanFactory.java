/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.jmx.server;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

/**
 * A factory for producing OddjobMBeans. The MBean names are just sequential formatted numbers.
 * The root name is created with the 0 number.
 * 
 */
public class OddjobMBeanFactory implements ServerSession {
	private static final Logger logger = Logger.getLogger(OddjobMBeanFactory.class);
	
	/** The server */
	private final MBeanServer server;
	
	/** Give each bean a serial number. */
	private int serial = 0;

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
	public OddjobMBeanFactory(MBeanServer server) {
		this.server = server;
	}
	
	/**
	 * Create an MBean and register with the server using the generated name.
	 * 
	 * @param obj The object the MBean is wrapping.
	 * @return The object name registered.
	 * @throws JMException If the MBean fails to register.
	 */
	public ObjectName createMBeanFor(Object obj, ServerContext context)
	throws JMException {
		ObjectName objName = null;
		synchronized (this) {
			objName = objectName(serial++);
		}
		
		names.put(obj, objName);
		
		OddjobMBean ojmb = new OddjobMBean(obj, this, context);
		server.registerMBean(ojmb, objName);
		
		mBeans.put(objName, ojmb);
		
		logger.debug("Created and registered [" + obj + "] as OddjobMBean [" + objName.toString() + "]");
		return objName;
	}
	
	/**
	 * Remove a bean from the server.
	 * 
	 * @param objName The bean.
	 * @throws JMException
	 */
	public void destroy(ObjectName objName) throws JMException {
		server.unregisterMBean(objName);
		OddjobMBean ojmb = mBeans.remove(objName);
		names.remove(ojmb.getNode());
		
		ojmb.destroy();
		
		logger.debug("Unregistered and destroyed OddjobMBean [" + objName.toString() + "]");
	}
	
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
		return names.get(object);
	}
	
	public Object objectFor(ObjectName objectName) {
		OddjobMBean mBean = mBeans.get(objectName);
		return mBean.getNode();
	}
}
