package org.oddjob.describe;

import java.util.Map;

import org.oddjob.Describable;

/**
 * Describe an object if it is {@link Describable}.
 * 
 * @author rob
 *
 */
public class DescribeableDescriber implements Describer {

	@Override
	public Map<String, String> describe(Object bean) {
		
		if (bean instanceof Describable) {
			return ((Describable) bean).describe();
		}
		else {
			return null;
		}
	}
	
}
