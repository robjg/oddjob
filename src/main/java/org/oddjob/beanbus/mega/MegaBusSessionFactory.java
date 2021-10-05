package org.oddjob.beanbus.mega;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.deploy.ClassPathDescriptorFactory;
import org.oddjob.arooa.deploy.LinkedDescriptor;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.arooa.standard.ExtendedTools;
import org.oddjob.beanbus.adapt.BusComponentResolver;

/**
 * Constructs an {@link ArooaSession} for a {@link MegaBeanBus}. 
 * 
 * @author rob
 *
 */
public class MegaBusSessionFactory {
	
	public static final String AROOA_DESCRIPTOR_RESOURCE = 
			"META-INF/beanbus-parts.xml";
	
	/**
	 * Create a session.
	 * 
	 * @param oddjob Used to find the id of Oddjob in any existing session
	 * if a persister is being used.
	 * 
	 * @return A session. Never null.
	 */
	public ArooaSession createSession(final ArooaSession existingSession,
			ClassLoader classLoader) {
		
		if (existingSession == null) {
			throw new NullPointerException("No existing ArooaSession.");
		}
		
		ComponentProxyResolver componentProxyResolver = null;
		
		ClassPathDescriptorFactory descriptorFactory = 
				new ClassPathDescriptorFactory();
		descriptorFactory.setResource(AROOA_DESCRIPTOR_RESOURCE);
		
		ArooaDescriptor descriptor = descriptorFactory.createDescriptor(
				classLoader);
		
		ArooaDescriptor existingDescriptor = 
				existingSession.getArooaDescriptor();
		
		descriptor = new LinkedDescriptor(descriptor, 
				existingDescriptor);
				
		ArooaTools tools = new ExtendedTools(
				existingSession.getTools(), 
				descriptor);
		
		componentProxyResolver = 
				new BusComponentResolver(
						existingSession.getComponentProxyResolver());
		
		final ArooaDescriptor finalDescriptor = descriptor;
    	final ArooaTools finalTools = tools;
    	final ComponentProxyResolver finalComponentProxyResolver =
    		componentProxyResolver;
    	
		return new ArooaSession() {
			@Override
			public ArooaDescriptor getArooaDescriptor() {
				return finalDescriptor;
			}
			@Override
			public ComponentPool getComponentPool() {
				return existingSession.getComponentPool();
			}
			@Override
			public BeanRegistry getBeanRegistry() {
				return existingSession.getBeanRegistry();
			}
			@Override
			public PropertyManager getPropertyManager() {
				return existingSession.getPropertyManager();
			}
			@Override
			public ArooaTools getTools() {
				return finalTools;
			}
			@Override
			public ComponentPersister getComponentPersister() {
				return null;
			}
			@Override
			public ComponentProxyResolver getComponentProxyResolver() {
				return finalComponentProxyResolver;
			}
		};
	}

}
