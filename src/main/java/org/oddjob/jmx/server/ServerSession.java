package org.oddjob.jmx.server;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.jmx.RemoteIdMappings;

import javax.management.JMException;

/**
 * Provide useful things to the interface handlers on the server side.
 * 
 * @author rob
 *
 */
public interface ServerSession extends RemoteIdMappings {

	/**
	 * Create an MBean.
	 * 
	 * @param child The Oddjob component.
	 * @param childContext The context.
	 * 
	 * @return The object id for the created MBean.
	 * 
	 * @throws JMException
	 */
	long createMBeanFor(Object child,
			ServerContext childContext)
	throws JMException;

	/**
	 * Destroy a server MBean.
	 * 
	 * @param childId The child name.
	 * 
	 * @throws JMException
	 */
	void destroy(long childId)
	throws JMException;

	/**
	 * Get the session used by the server.
	 * 
	 * @return The session.
	 */
	ArooaSession getArooaSession();
}
