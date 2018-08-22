package org.oddjob;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.deploy.ClassPathDescriptorFactory;

/**
 * Create Oddjobs main {@link ArooaDescriptor}.
 * 
 * @author rob
 *
 */
public class OddjobDescriptorFactory implements ArooaDescriptorFactory {

	@Override
	public ArooaDescriptor createDescriptor(ClassLoader classLoader) {

		if (classLoader == null) {
			classLoader = getClass().getClassLoader();
		}
		
		ClassPathDescriptorFactory factory = 
			new ClassPathDescriptorFactory();

		ArooaDescriptor descriptor = factory.createDescriptor(classLoader);		
		
		if (descriptor == null) {
			throw new NullPointerException(
					"No descriptors found with class loader " +
					classLoader);
		}
		
		return descriptor;
	}
}
