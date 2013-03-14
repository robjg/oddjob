package org.oddjob.beanbus.mega;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.Describeable;
import org.oddjob.Iconic;
import org.oddjob.arooa.life.ArooaLifeAware;
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
public class CollectionProxyGenerator<E> extends ProxyGenerator<Collection<E>> {

	/**
	 * Generate the collection.
	 * 
	 * @param collection
	 * @param classLoader
	 * 
	 * @return The collection.
	 */
	public Object generate(Collection<E> collection, ClassLoader classLoader) {
		return generate(collection, new WrapperFactory<Collection<E>>() {
			@Override
			public Class<?>[] wrappingInterfacesFor(Collection<E> wrapped) {
					
				Set<Class<?>> interfaces = new HashSet<Class<?>>();
				interfaces.add(Object.class);
				interfaces.add(ArooaSessionAware.class);
				interfaces.add(ArooaLifeAware.class);
				interfaces.add(DynaBean.class);
				interfaces.add(LogEnabled.class);
				interfaces.add(Describeable.class);
				interfaces.add(Iconic.class);
				interfaces.add(BusPart.class);
				interfaces.add(Collection.class);
				
				return (Class[]) interfaces.toArray(
						new Class[interfaces.size()]);		
			}
			@Override
			public ComponentWrapper wrapperFor(Collection<E> wrapped, Object proxy) {
				CollectionWrapper<E> wrapper = new CollectionWrapper<E>(wrapped, proxy);
				return wrapper;
			}
		}, classLoader);
	}
}
