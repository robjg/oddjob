package org.oddjob.framework.adapt.job;

import java.util.concurrent.Callable;

import org.oddjob.framework.adapt.ComponentWrapper;
import org.oddjob.framework.adapt.ProxyGenerator;

/**
 * Generate an Oddjob proxy for a Callable.
 * 
 * @author rob
 *
 */
public class CallableProxyGenerator extends ProxyGenerator<Callable<?>> {

	/**
	 * Generate the proxy.
	 * 
	 * @param callable
	 * @param classLoader
	 * 
	 * @return A proxy.
	 */
	public Object generate(Callable<?> callable, ClassLoader classLoader) {
		return generate(callable, new BaseWrapperFactory<Callable<?>>() {
			@Override
			public ComponentWrapper wrapperFor(Callable<?> wrapped, Object proxy) {
				RunnableWrapper wrapper = new RunnableWrapper(wrapped, proxy);
				return wrapper;
			}
		}, classLoader);
	}
}
