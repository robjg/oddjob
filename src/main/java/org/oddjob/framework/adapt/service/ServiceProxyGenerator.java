package org.oddjob.framework.adapt.service;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.*;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.framework.Transient;
import org.oddjob.framework.adapt.ComponentWrapper;
import org.oddjob.framework.adapt.ProxyGenerator;
import org.oddjob.framework.adapt.WrapperFactory;
import org.oddjob.logging.LogEnabled;

import java.util.HashSet;
import java.util.Set;

/**
 * Generate a Proxy for a service.
 * <p>
 * The service Proxy doesn't implement {@link Forceable} and all
 * instances are {@link Transient}.
 * 
 * @author rob
 *
 */
public class ServiceProxyGenerator extends ProxyGenerator<ServiceAdaptor> {

	/**
	 * Generate the proxy.
	 * 
	 * @param service
	 * @param classLoader
	 * 
	 * @return The proxy.
	 */
	public Object generate(ServiceAdaptor service, ClassLoader classLoader) {
		return generate(service, new WrapperFactory<ServiceAdaptor>() {
			@Override
			public Class<?>[] wrappingInterfacesFor(ServiceAdaptor wrapped) {
					
				Set<Class<?>> interfaces = new HashSet<>();
				interfaces.add(Object.class);
				interfaces.add(ArooaSessionAware.class);
				interfaces.add(ArooaContextAware.class);
				interfaces.add(Stateful.class);
				interfaces.add(Resettable.class);
				interfaces.add(Forceable.class);
				interfaces.add(DynaBean.class);
				interfaces.add(Stoppable.class);
				interfaces.add(Iconic.class);
				interfaces.add(Runnable.class);
				interfaces.add(LogEnabled.class);
				interfaces.add(Transient.class);
				interfaces.add(Describable.class);
				
				return (Class[]) interfaces.toArray(
						new Class[interfaces.size()]);		
			}
			@Override
			public ComponentWrapper wrapperFor(ServiceAdaptor wrapped, Object proxy) {
				ServiceWrapper wrapper = new ServiceWrapper(wrapped, proxy);
				return wrapper;
			}
		}, classLoader);
	}
}
