package org.oddjob;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.deploy.ClassPathDescriptorFactory;

public class OddjobDescriptorFactory implements ArooaDescriptorFactory {

	@Override
	public ArooaDescriptor createDescriptor(ClassLoader classLoader) {

		if (classLoader == null) {
			classLoader = getClass().getClassLoader();
		}
		
		ClassPathDescriptorFactory factory = 
			new ClassPathDescriptorFactory();

		return factory.createDescriptor(classLoader);		
	}
}
