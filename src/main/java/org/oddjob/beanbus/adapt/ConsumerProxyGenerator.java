package org.oddjob.beanbus.adapt;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.*;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.framework.AsyncService;
import org.oddjob.framework.adapt.ComponentWrapper;
import org.oddjob.framework.adapt.ProxyGenerator;
import org.oddjob.framework.adapt.WrapperFactory;
import org.oddjob.framework.adapt.service.ServiceAdaptor;
import org.oddjob.framework.adapt.service.ServiceStrategies;
import org.oddjob.logging.LogEnabled;

import java.beans.ExceptionListener;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Generate a Proxy for a collection.
 * <p>
 * 
 * @author rob
 *
 */
public class ConsumerProxyGenerator<E> extends ProxyGenerator<Consumer<E>> {

	private final ArooaSession session;

	public ConsumerProxyGenerator(ArooaSession session) {
		this.session = session;
	}

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
				interfaces.add(ArooaContextAware.class);
				interfaces.add(Stateful.class);
				interfaces.add(Resettable.class);
				interfaces.add(DynaBean.class);
				interfaces.add(Stoppable.class);
				interfaces.add(Iconic.class);
				interfaces.add(Runnable.class);
				interfaces.add(LogEnabled.class);
				interfaces.add(Describable.class);
				interfaces.add(Consumer.class);
				
				return (Class<?>[]) interfaces.toArray(new Class[0]);
			}
			@Override
			public ComponentWrapper wrapperFor(Consumer<E> wrapped, Object proxy) {
				ServiceAdaptor serviceAdaptor = new ServiceStrategies()
						.adapt(wrapped, session)
						.orElseGet(() -> new ServiceAdaptor() {
							@Override
							public void acceptExceptionListener(ExceptionListener exceptionListener) {
							}

							@Override
							public void start() {
							}

							@Override
							public void stop() {
							}

							@Override
							public Object getComponent() {
								return wrapped;
							}

							@Override
							public Optional<AsyncService> asAsync() {
								return Optional.empty();
							}
						});

				return new ConsumerWrapper<>(serviceAdaptor, wrapped, proxy);
			}
		}, classLoader);
	}
}
