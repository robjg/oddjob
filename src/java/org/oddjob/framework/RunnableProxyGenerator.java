package org.oddjob.framework;


/**
 * Proxy generator for a Runnable.
 * 
 * @author rob
 *
 */
public class RunnableProxyGenerator extends ProxyGenerator<Runnable> {

	/**
	 * Generate the proxy.
	 * 
	 * @param runnable
	 * @param classLoader
	 * 
	 * @return A proxy.
	 */
	public Object generate(Runnable runnable, ClassLoader classLoader) {
		return generate(runnable, new BaseWrapperFactory<Runnable>() {
			@Override
			public ComponentWrapper wrapperFor(Runnable wrapped, Object proxy) {
				RunnableWrapper runnable = new RunnableWrapper(wrapped, proxy);
				return runnable;
			}
		}, classLoader);
	}
}
