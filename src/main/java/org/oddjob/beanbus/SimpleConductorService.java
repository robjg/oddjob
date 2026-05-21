package org.oddjob.beanbus;


import java.lang.reflect.Type;

public class SimpleConductorService implements ConductorService {

	private final BusConductor beanConductor;
	
	public SimpleConductorService(BusConductor busConductor) {
		if (busConductor == null) {
			throw new NullPointerException("Bus conductor is null.");
		}
		this.beanConductor = busConductor;
	}
	
	public SimpleConductorService(ConductorServiceProvider delegate) {
		this(delegate.getServices().getService(CONDUCTOR_SERVICE_NAME));
	}
	
	@Override
	public String serviceNameFor(Type theClass, String flavour) {
		if (BusConductor.class == theClass) {
			return CONDUCTOR_SERVICE_NAME;
		}
		else {
			return null;
		}
	}
	
	@Override
	public BusConductor getService(String serviceName)
			throws IllegalArgumentException {
		if (CONDUCTOR_SERVICE_NAME.equals(serviceName)) {
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
