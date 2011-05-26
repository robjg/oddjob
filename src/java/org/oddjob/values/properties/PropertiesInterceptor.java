package org.oddjob.values.properties;

import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.ParsingInterceptor;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.SessionOverrideContext;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.runtime.PropertyManager;
import org.oddjob.arooa.standard.StandardPropertyManager;

public class PropertiesInterceptor implements ParsingInterceptor {

	@Override
	public ArooaContext intercept(ArooaContext suggestedContext)
			throws ArooaConfigurationException {
		if (! (suggestedContext.getSession() instanceof Session)) {
			return new SessionOverrideContext(suggestedContext, 
					new Session(suggestedContext.getSession()));
		}
		else {
			return suggestedContext;
		}
	}
	
	public static class Session implements ArooaSession {
		
		private final PropertyManager propertyManager;

		private final ArooaSession session;
		
		public Session(ArooaSession session) {
			this.propertyManager = new StandardPropertyManager(
				session.getPropertyManager());
			this.session = session;
		}
		
		@Override
		public ArooaDescriptor getArooaDescriptor() {
			return session.getArooaDescriptor();
		}

		@Override
		public ComponentPool getComponentPool() {
			return session.getComponentPool();
		}

		@Override
		public BeanRegistry getBeanRegistry() {
			return session.getBeanRegistry();
		}

		@Override
		public PropertyManager getPropertyManager() {
			return propertyManager;
		}

		@Override
		public ArooaTools getTools() {
			return session.getTools();
		}

		@Override
		public ComponentPersister getComponentPersister() {
			return session.getComponentPersister();
		}

		@Override
		public ComponentProxyResolver getComponentProxyResolver() {
			return session.getComponentProxyResolver();
		}
	}
}
