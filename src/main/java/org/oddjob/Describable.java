/*
 * (c) Rob Gordon 2005
 */
package org.oddjob;

import java.util.Map;

/**
 * Object that implement this interface provide their own
 * description of what should appear on the properties panel
 * of the monitor.
 * 
 * @author rob
 *
 */
public interface Describable {

	/**
	 * Provides the properties.
	 * 
	 * @return A map of property values. Must not be null.
	 */
	Map<String, String> describe();
}
