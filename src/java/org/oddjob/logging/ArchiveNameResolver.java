package org.oddjob.logging;

/**
 * A Resolver is able to resolve the logger name for a 
 * given component. This may involve inspecting the logger property or
 * building a logger name from a components remote url and logger
 * property.
 *
 * @see LogEnabled
 */
public interface ArchiveNameResolver {
	
	/**
	 * Resolve the logger name for the component.
	 * 
	 * @param component The component.
	 * 
	 * @return The logger name. Will be null if no logger
	 * is available for the component.
	 */
	public String resolveName(Object component);
}