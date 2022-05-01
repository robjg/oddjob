package org.oddjob.beanbus.bus;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.beanbus.adapt.BusComponentResolver;

/**
 * Constructs an {@link ArooaSession} for a {@link BasicBusService}.
 * 
 * @author rob
 *
 */
public class BusSessionFactory {
	
	/**
	 * Create a session.
	 * 
	 * @param existingSession Used to find the id of Oddjob in any existing session
	 * if a persister is being used.
	 * @param classLoader The classloader.
	 *
	 * @return A session. Never null.
	 */
	public ArooaSession createSession(final ArooaSession existingSession,
			ClassLoader classLoader) {
		
		if (existingSession == null) {
			throw new NullPointerException("No existing ArooaSession.");
		}
		
		ComponentProxyResolver componentResolver =
				new BusComponentResolver(
						existingSession.getComponentProxyResolver());
		
		return new ArooaSession() {
			@Override
			public ArooaDescriptor getArooaDescriptor() {
				return existingSession.getArooaDescriptor();
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
				return existingSession.getTools();
			}
			@Override
			public ComponentPersister getComponentPersister() {
				return null;
			}
			@Override
			public ComponentProxyResolver getComponentProxyResolver() {
				return componentResolver;
			}
		};
	}

}
