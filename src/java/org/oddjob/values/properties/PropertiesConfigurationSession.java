package org.oddjob.values.properties;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.arooa.standard.StandardPropertyManager;

/**
 * A version of an {@link ArooaSession} that creates it's own copy
 * of a {@link PropertyManager} so that it can resolve properties during
 * configuration of the {@link PropertyType} and {@link PropertyJob}.
 * 
 * @author rob
 *
 */
public class PropertiesConfigurationSession implements ArooaSession {

	private final ArooaSession original;
	
	private final PropertyManager propertyManager;
	
	public PropertiesConfigurationSession(ArooaSession original) {
		this.original = original;
		
		this.propertyManager = new StandardPropertyManager(
				original.getPropertyManager());
	}
	
	@Override
	public ArooaDescriptor getArooaDescriptor() {
		return original.getArooaDescriptor();
	}
	
	@Override
	public BeanRegistry getBeanRegistry() {
		return original.getBeanRegistry();
	}
	
	@Override
	public PropertyManager getPropertyManager() {
		return propertyManager;
	}
	
	@Override
	public ComponentPersister getComponentPersister() {
		return original.getComponentPersister();
	}
	
	@Override
	public ComponentProxyResolver getComponentProxyResolver() {
		return original.getComponentProxyResolver();
	}
	
	@Override
	public ComponentPool getComponentPool() {
		return original.getComponentPool();
	}
	
	@Override
	public ArooaTools getTools() {
		return original.getTools();
	}
	
	public ArooaSession getOriginal() {
		return original;
	}
}
