package org.oddjob.beanbus.mega;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.Describable;
import org.oddjob.Iconic;
import org.oddjob.arooa.life.ArooaLifeAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.framework.adapt.ComponentWrapper;
import org.oddjob.framework.adapt.ProxyGenerator;
import org.oddjob.framework.adapt.WrapperFactory;
import org.oddjob.logging.LogEnabled;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Generate a Proxy for a collection.
 * <p>
 * 
 * @author rob
 *
 */
public class CollectionProxyGenerator<E> extends ProxyGenerator<Consumer<E>> {

	/**
	 * Generate the collection.
	 * 
	 * @param collection
	 * @param classLoader
	 * 
	 * @return The collection.
	 */
	public Object generate(Consumer<E> collection, ClassLoader classLoader) {
		return generate(collection, new WrapperFactory<Consumer<E>>() {
			@Override
			public Class<?>[] wrappingInterfacesFor(Consumer<E> wrapped) {
					
				Set<Class<?>> interfaces = new HashSet<>();
				interfaces.add(Object.class);
				interfaces.add(ArooaSessionAware.class);
				interfaces.add(ArooaLifeAware.class);
				interfaces.add(DynaBean.class);
				interfaces.add(LogEnabled.class);
				interfaces.add(Describable.class);
				interfaces.add(Iconic.class);
				interfaces.add(BusPart.class);
				interfaces.add(Consumer.class);
				
				return (Class<?>[]) interfaces.toArray(new Class[0]);
			}
			@Override
			public ComponentWrapper wrapperFor(Consumer<E> wrapped, Object proxy) {
				return new CollectionWrapper<>(wrapped, proxy);
			}
		}, classLoader);
	}
}
