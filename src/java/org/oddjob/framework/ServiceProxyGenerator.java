package org.oddjob.framework;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.Forceable;
import org.oddjob.Iconic;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.logging.LogEnabled;

/**
 * Generate a Proxy for a service.
 * <p>
 * The service Proxy doesn't implement {@link Forceable} and all
 * instances are {@link Transient}.
 * 
 * @author rob
 *
 */
public class ServiceProxyGenerator extends ProxyGenerator<Service> {

	public Object generate(Service service, ClassLoader classLoader) {
		return generate(service, new WrapperFactory<Service>() {
			@Override
			public Class<?>[] wrappingInterfacesFor(Service wrapped) {
					
				Set<Class<?>> interfaces = new HashSet<Class<?>>();
				interfaces.add(Object.class);
				interfaces.add(ArooaContextAware.class);
				interfaces.add(Stateful.class);
				interfaces.add(Resetable.class);
				interfaces.add(Forceable.class);
				interfaces.add(DynaBean.class);
				interfaces.add(Stoppable.class);
				interfaces.add(Iconic.class);
				interfaces.add(Runnable.class);
				interfaces.add(LogEnabled.class);
				interfaces.add(Transient.class);

				return (Class[]) interfaces.toArray(new Class[0]);		
			}
			@Override
			public ComponentWrapper wrapperFor(Service wrapped, Object proxy) {
				return new ServiceWrapper(wrapped, proxy);
			}
		}, classLoader);
	}
}
