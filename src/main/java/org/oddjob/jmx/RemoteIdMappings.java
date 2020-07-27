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
	 * @param object The component.
	 * 
	 * @return The remote Id. <0 if the Object isn't found.
	 */
	long idFor(Object object);
	
	/**
	 * Get the component for the given remote Id. On the server this will be the original component,
	 * on the client this will ba a proxy.
	 * 
	 * @param remoteId The remote Id.
	 * 
	 * @return The component. Null if none exists for the given id.
	 */
	Object objectFor(long remoteId);
}
