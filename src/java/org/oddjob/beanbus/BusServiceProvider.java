package org.oddjob.beanbus;

import org.oddjob.arooa.registry.ServiceProvider;

/**
 * Something that provides a {@link BusService}.
 * 
 * @author rob
 *
 */
public interface BusServiceProvider extends ServiceProvider {

	 @Override
	public BusService getServices();
}
