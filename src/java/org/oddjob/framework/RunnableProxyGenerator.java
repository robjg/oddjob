package org.oddjob.framework;

public class RunnableProxyGenerator extends ProxyGenerator<Runnable> {

	public Object generate(Runnable runnable, ClassLoader classLoader) {
		return generate(runnable, new BaseWrapperFactory<Runnable>() {
			@Override
			public ComponentWrapper wrapperFor(Runnable wrapped, Object proxy) {
				return new RunnableWrapper(wrapped, proxy);
			}
		}, classLoader);
	}
}
