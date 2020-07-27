/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.jmx.server;

import org.oddjob.arooa.ArooaSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A factory for producing OddjobMBeans. The MBean names are just sequential formatted numbers.
 * The root name is created with the 0 number.
 * 
 */
public class OddjobMBeanFactory implements ServerSession {
	private static final Logger logger = LoggerFactory.getLogger(OddjobMBeanFactory.class);
	
	/** The server */
	private final MBeanServer server;
	
	private final ArooaSession session;
	
	/** Give each bean a serial number. */
	private final AtomicLong serial = new AtomicLong();

	/** Keep track of MBeans by ObjectName so that we can destroy them 
	 * when they're finished. */
	private final Map<Long, OddjobMBean> mBeans = new HashMap<>();
	
	private final Map<Object, Long> names = new HashMap<>();
	
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
	@Override
	public long createMBeanFor(Object object, ServerContext context)
	throws JMException {

		long objectId = serial.getAndIncrement();

		OddjobMBean ojmb = new OddjobMBean(object, objectId, this, context);
		ObjectName objName = ojmb.getObjectName();

		server.registerMBean(ojmb, objName);
		
		synchronized (this) {
			names.put(object, objectId);
			mBeans.put(objectId, ojmb);
		}
		
		logger.debug("Created and registered [" + object + "] as OddjobMBean [" + objName.toString() + "]");

		return objectId;
	}
	
	/**
	 * Remove a bean from the server.
	 * 
	 * @param objectId The bean.
	 * @throws JMException
	 */
	@Override
	public void destroy(long objectId) throws JMException {

		OddjobMBean ojmb;

		synchronized (this) {
			ojmb = mBeans.remove(objectId);
			if (ojmb == null) {
				throw new IllegalStateException("No MBean named " + objectId);
			}
			names.remove(ojmb.getNode());
		}
		
		ojmb.destroy();

		ObjectName objectName = objectName(objectId);
		server.unregisterMBean(objectName);
		
		logger.debug("Unregistered and destroyed [" + ojmb + "]");
	}
	
	/**
	 * Helper function to build the object name from the sequence number.
	 * 
	 * @param sequence The object sequence number.
	 * @return A JMX object name.
	 */
	public static ObjectName objectName(long sequence) {
		try {
			NumberFormat f = new DecimalFormat("00000000");
			String uid = f.format(sequence);
			return new ObjectName("oddjob", "uid", uid);
		}
		catch (MalformedObjectNameException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public long idFor(Object object) {
		synchronized (this) {
			return Optional.ofNullable(names.get(object)).orElse(-1L);
		}
	}

	@Override
	public Object objectFor(long remoteId) {
		OddjobMBean mBean;
		synchronized (this) {
			mBean = mBeans.get(remoteId);
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
