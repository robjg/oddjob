package org.oddjob.jmx;

/**
 * Provide a two way Object to Remote Id mapping.
 * 
 * @author rob
 *
 */
public interface RemoteIdMappings {

	/**
	 * Get the remote Id for the give component object.
	 * 
	 * @param object The proxy.
	 * 
	 * @return The remote Id. <0 if the Object isn't found.
	 */
	long nameFor(Object object);
	
	/**
	 * Get the client side component for the given remote Id.
	 * 
	 * @param remoteId The remote Id.
	 * 
	 * @return The proxy. Null if none exsists for the given id.
	 */
	Object objectFor(long remoteId);
}
