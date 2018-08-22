package org.oddjob.jmx;

import javax.management.ObjectName;

/**
 * Provide a two way Object to ObjectName mapping.
 * 
 * @author rob
 *
 */
public interface ObjectNames {

	/**
	 * Get the object name for the give component object.
	 * 
	 * @param object The proxy.
	 * 
	 * @return The name.
	 */
	ObjectName nameFor(Object object);
	
	/**
	 * Get the client side component for the given object name.
	 * 
	 * @param objectName The object name.
	 * 
	 * @return The proxy. Null if none exsists for the given name.
	 */
	Object objectFor(ObjectName objectName);
}
