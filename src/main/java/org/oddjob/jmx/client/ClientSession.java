package org.oddjob.jmx.client;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.jmx.RemoteIdMappings;
import org.slf4j.Logger;

/**
 * A facility shared by all client side handlers.
 * 
 * @author rob
 *
 */
public interface ClientSession extends RemoteIdMappings {
	
	/**
	 * Create or find a previously created proxy for
	 * the JMX object name.
	 * 
	 * @param objectId The JMX object name.
	 * 
	 * @return The proxy. Never null.
	 */
	Object create(long objectId);
	
	/**
	 * Destroy a client side proxy. Allows handlers to 
	 * free resources.
	 * 
	 * @param proxy
	 */
	void destroy(Object proxy);
	
	
	/**
	 * Get the {@link ArooaSession} the client was
	 * created with.
	 * 
	 * @return
	 */
	ArooaSession getArooaSession();
	
	/**
	 * The Client JOb Logger, visible in explorer.
	 * 
	 * @return
	 */
	Logger logger();
	
	/**
	 * Destroy all proxies on the client side.
	 */
	void destroyAll();


	ClientInterfaceManagerFactory getInterfaceManagerFactory();

}
