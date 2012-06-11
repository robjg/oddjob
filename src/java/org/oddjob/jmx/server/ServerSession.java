package org.oddjob.jmx.server;

import javax.management.JMException;
import javax.management.ObjectName;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.jmx.ObjectNames;

/**
 * Provide useful things to the interface handlers on the server side.
 * 
 * @author rob
 *
 */
public interface ServerSession extends ObjectNames {

	/**
	 * Create an MBean.
	 * 
	 * @param child The Oddjob component.
	 * @param childContext The context.
	 * 
	 * @return The object name for the created MBean.
	 * 
	 * @throws JMException
	 */
	public ObjectName createMBeanFor(Object child, 
			ServerContext childContext)
	throws JMException;

	/**
	 * Destroy a server MBean.
	 * 
	 * @param childName The child name.
	 * 
	 * @throws JMException
	 */
	public void destroy(ObjectName childName)
	throws JMException;

	/**
	 * Get the session used by the server.
	 * 
	 * @return The session.
	 */
	public ArooaSession getArooaSession();
}
