package org.oddjob.beanbus;

import org.oddjob.arooa.registry.ServiceProvider;

public interface BusServiceProvider extends ServiceProvider {

	 @Override
	public BusService getServices();
}
