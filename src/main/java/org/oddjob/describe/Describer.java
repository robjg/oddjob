package org.oddjob.describe;

import java.util.Map;

/**
 * Something that can describe the properties of a component.
 * 
 * @author rob
 *
 */
public interface Describer {

	/**
	 * Describe a bean.
	 * 
	 * @param bean The bean.
	 * 
	 * @return Return a map which is a description of the properties.
	 */
	public Map<String, String> describe(Object bean);
}
