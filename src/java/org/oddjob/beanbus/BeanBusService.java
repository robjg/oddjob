package org.oddjob.beanbus;

import org.oddjob.arooa.registry.Services;

public interface BeanBusService extends Services {

	public static final String BEAN_BUS_SERVICE_NAME = "BeanBus";
	
	@Override
	public BusConductor getService(String serviceName)
			throws IllegalArgumentException;
	
	
}