package org.oddjob.beanbus;

import org.oddjob.arooa.registry.Services;

/**
 * Provide a service for buses. The interaction with the bus
 * service is the {@link BusConductor}.
 * 
 * @author rob
 *
 */
public interface BusService extends Services {

	public static final String BEAN_BUS_SERVICE_NAME = "BeanBus";
		
	@Override
	public BusConductor getService(String serviceName);
}
