package org.oddjob.beanbus.mega;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.Describeable;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.framework.ComponentWrapper;
import org.oddjob.framework.ProxyGenerator;
import org.oddjob.framework.WrapperFactory;
import org.oddjob.logging.LogEnabled;

/**
 * Generate a Proxy for a collection.
 * <p>
 * 
 * @author rob
 *
 */
public class CollectionProxyGenerator extends ProxyGenerator<Collection<?>> {

	/**
	 * Generate the collection.
	 * 
	 * @param collection
	 * @param classLoader
	 * 
	 * @return The collection.
	 */
	public Object generate(Collection<?> collection, ClassLoader classLoader) {
		return generate(collection, new WrapperFactory<Collection<?>>() {
			@Override
			public Class<?>[] wrappingInterfacesFor(Collection<?> wrapped) {
					
				Set<Class<?>> interfaces = new HashSet<Class<?>>();
				interfaces.add(Object.class);
				interfaces.add(ArooaSessionAware.class);
				interfaces.add(DynaBean.class);
				interfaces.add(LogEnabled.class);
				interfaces.add(Describeable.class);
				
				return (Class[]) interfaces.toArray(
						new Class[interfaces.size()]);		
			}
			@Override
			public ComponentWrapper wrapperFor(Collection<?> wrapped, Object proxy) {
				CollectionWrapper wrapper = new CollectionWrapper(wrapped, proxy);
				return wrapper;
			}
		}, classLoader);
	}
}
