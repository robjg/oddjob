package org.oddjob.beanbus;


public class SimpleBusService implements BusService {

	private final BusConductor beanConductor;
	
	public SimpleBusService(BusConductor busConductor) {
		if (busConductor == null) {
			throw new NullPointerException("Bus conductor is null.");
		}
		this.beanConductor = busConductor;
	}
	
	public SimpleBusService(BusServiceProvider delegate) {
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
