package org.oddjob;

/**
 * Values that the Oddjob Inheritance property can take.
 * 
 * @author rob
 *
 */
public enum OddjobInheritance {

	/**
	 * No values or properties are automatically inherited.
	 */
	NONE,
	
	/**
	 * All properties are inherited. Only properties are inherited, values
	 * must be exported explicitly using the export property.
	 */
	PROPERTIES,
	
	/**
	 * All properties and values are shared between the parent and child
	 * Oddjobs. Any properties or values set in the child will be visible
	 * in the parent. This setting is particularly useful for shared common
	 * configuration.
	 */
	SHARED,
}
