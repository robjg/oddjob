package org.oddjob.beanbus;

import org.oddjob.arooa.registry.Services;

public class BusService implements Services {

	public static final String BEAN_BUS_SERVICE_NAME = "BeanBus";
	
	private final BusConductor beanConductor;
	
	public BusService(BusConductor busConductor) {
		if (busConductor == null) {
			throw new NullPointerException("Bus conductor is null.");
		}
		this.beanConductor = busConductor;
	}
	
	public BusService(BusServiceProvider delegate) {
		this(delegate.getServices().getService(BEAN_BUS_SERVICE_NAME));
	}
	
	@Override
	public String serviceNameFor(Class<?> theClass, String flavour) {
		if (BusConductor.class == theClass) {
			return BEAN_BUS_SERVICE_NAME;
		}
		else {
			return null;
		}
	}
	
	@Override
	public BusConductor getService(String serviceName)
			throws IllegalArgumentException {
		if (BEAN_BUS_SERVICE_NAME.equals(serviceName)) {
			return beanConductor;
		}
		else {
			return null;
		}
		
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
