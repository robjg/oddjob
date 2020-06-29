package org.oddjob.jmx.client;

/**
 * Something that can be destroyed. Intended for handlers that need to
 * clean up when client proxies are destroyed.
 * 
 * @author rob
 *
 */
public interface Destroyable {

	/**
	 * destroy the client side object.
	 */
	void destroy();
	
}
