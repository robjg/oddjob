package org.oddjob.describe;

import java.util.Map;

import org.oddjob.Describeable;

/**
 * Describe an object if it is {@link Describeable}.
 * 
 * @author rob
 *
 */
public class DescribeableDescriber implements Describer {

	@Override
	public Map<String, String> describe(Object bean) {
		
		if (bean instanceof Describeable) {
			return ((Describeable) bean).describe();
		}
		else {
			return null;
		}
	}
	
}
