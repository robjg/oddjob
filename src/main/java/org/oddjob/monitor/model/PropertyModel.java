/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import java.util.Map;
import java.util.Observable;

/**
 * A Model for job properties.
 * 
 * @author Rob Gordon.
 *
 */
public class PropertyModel extends Observable {

	/** The map of properties. */
	private Map<String, String> properties;
	
	/**
	 * Called to update the properties.
	 * 
	 * @param map A <String, String> map of the properties.
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Get the property map.
	 * 
	 * @return A <String, String> map of properties.
	 */
	public Map<String, String> getProperties() {
		return properties;
	}
}
