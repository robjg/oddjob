package org.oddjob.beanbus;

import org.oddjob.arooa.registry.Services;

/**
 * Provide a service for buses. The interaction with the bus
 * service is the {@link BusConductor}.
 * 
 * @author rob
 *
 */
public interface ConductorService extends Services {

	String CONDUCTOR_SERVICE_NAME = "BusConductor";
		
	@Override
	BusConductor getService(String serviceName);
}
