package org.oddjob.framework;

import java.util.concurrent.Callable;

public class CallableProxyGenerator extends ProxyGenerator<Callable<?>> {

	public Object generate(Callable<?> callable, ClassLoader classLoader) {
		return generate(callable, new BaseWrapperFactory<Callable<?>>() {
			@Override
			public ComponentWrapper wrapperFor(Callable<?> wrapped, Object proxy) {
				return new RunnableWrapper(wrapped, proxy);
			}
		}, classLoader);
	}
}
