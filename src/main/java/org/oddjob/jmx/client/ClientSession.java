package org.oddjob.jmx.client;

import javax.management.ObjectName;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.jmx.ObjectNames;
import org.slf4j.Logger;

/**
 * A facility shared by all client side handlers.
 * 
 * @author rob
 *
 */
public interface ClientSession extends ObjectNames {
	
	/**
	 * Create or find a previously created proxy for
	 * the JMX object name.
	 * 
	 * @param objectName The JMX object name.
	 * 
	 * @return The proxy. Never null.
	 */
	public Object create(ObjectName objectName);
	
	/**
	 * Destroy a client side proxy. Allows handlers to 
	 * free resources.
	 * 
	 * @param proxy
	 */
	public void destroy(Object proxy);
	
	
	/**
	 * Get the {@link ArooaSession} the client was
	 * created with.
	 * 
	 * @return
	 */
	public ArooaSession getArooaSession();
	
	/**
	 * The Client JOb Logger, visible in explorer.
	 * 
	 * @return
	 */
	public Logger logger();
	
	/**
	 * Destroy all proxies on the client side.
	 */
	public void destroyAll();
	
}
