package org.oddjob.framework.adapt.job;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.*;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.framework.Transient;
import org.oddjob.framework.adapt.ComponentWrapper;
import org.oddjob.framework.adapt.ProxyGenerator;
import org.oddjob.framework.adapt.WrapperFactory;
import org.oddjob.logging.LogEnabled;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Generate a Proxy for a Job.
 * <p>
 *
 * @author rob
 *
 */
public class JobProxyGenerator extends ProxyGenerator<JobAdaptor> {

	/**
	 * Generate the proxy.
	 * 
	 * @param jobAdaptor
	 * @param classLoader
	 * 
	 * @return The proxy.
	 */
	public Object generate(JobAdaptor jobAdaptor, ClassLoader classLoader) {
		return generate(jobAdaptor, new WrapperFactory<JobAdaptor>() {

			@Override
			public Class<?>[] wrappingInterfacesFor(JobAdaptor adaptor) {

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
				interfaces.add(Describable.class);

				if (!(adaptor.getComponent() instanceof Serializable)) {
					interfaces.add(Transient.class);
				}

				return (Class<?>[]) interfaces.toArray(new Class[0]);
			}

			@Override
			public ComponentWrapper wrapperFor(JobAdaptor wrapped, Object proxy) {
				return new JobWrapper(wrapped, proxy);
			}
		}, classLoader);
	}
}
