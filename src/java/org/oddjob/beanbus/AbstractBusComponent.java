package org.oddjob.beanbus;

import org.apache.log4j.Logger;
import org.oddjob.arooa.registry.ServiceProvider;
import org.oddjob.arooa.registry.Services;

/**
 * A base class for Jobs and Services that provide an {@link BeanBus}.
 * 
 * @author rob
 *
 * @param <T> The type of beans on the bus.
 */
abstract public class AbstractBusComponent<T> 
implements BeanBusService, ServiceProvider {
	
	private static final Logger logger = Logger.getLogger(AbstractBusComponent.class);
	
	private final BasicBeanBus<T> beanBus = new BasicBeanBus<T>(
			new BeanBusCommand() {
				@Override
				public void run() throws BusCrashException {
					requestStopBus();
				}
			});
	
	protected void startBus() throws BusCrashException {
		logger.info("Starting Bus.");
		beanBus.startBus();
	}
	
	protected void accept(T bean) throws BadBeanException, BusCrashException {
		beanBus.accept(bean);
	}
	
	protected void stopBus() throws BusCrashException {
		logger.info("Stopping Bus.");
		beanBus.stopBus();
	}

	protected abstract void requestStopBus() throws BusCrashException;
	
	@Override
	public BusConductor getService(String serviceName)
			throws IllegalArgumentException {
		if (BEAN_BUS_SERVICE_NAME.equals(serviceName)) {
			return beanBus;
		}
		else {
			return null;
		}
	}
	
	@Override
	public String serviceNameFor(Class<?> theClass, String flavour) {
		if (BeanBusService.class == theClass) {
			return BEAN_BUS_SERVICE_NAME;
		}
		else {
			return null;
		}
	}
	
	@Override
	public Services getServices() {
		return this;
	}
		
	/**
	 * Set the destination.
	 * 
	 * @param to
	 */
	public void setTo(Destination<? super T> to) {
		beanBus.setTo(to);
	}
	
	public Destination<? super T> getTo() {
		return beanBus.getTo();
	}
	
}
