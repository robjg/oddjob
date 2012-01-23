package org.oddjob.framework;

public class ServiceProxyGenerator extends ProxyGenerator<Service> {

	public Object generate(Service service, ClassLoader classLoader) {
		return generate(service, new BaseWrapperFactory<Service>() {
			@Override
			public ComponentWrapper wrapperFor(Service wrapped, Object proxy) {
				return new ServiceWrapper(wrapped, proxy);
			}
		}, classLoader);
	}
}
