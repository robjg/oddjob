package org.oddjob.beanbus;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.oddjob.arooa.registry.ServiceProvider;

/**
 * A base class for Jobs and Services that provide an {@link BeanBus}.
 * <p>
 * Implementations must ensure {@link #startBus() and {@link stopBus()}
 * are called and must provide a {@link #stopTheBus()} method.
 * 
 * 
 * @author rob
 *
 * @param <T> The type of beans on the bus.
 */
abstract public class AbstractBusComponent<T> 
implements BusServiceProvider, ServiceProvider {
	
	private static final Logger logger = Logger.getLogger(AbstractBusComponent.class);
	
	private final BasicBeanBus<T> beanBus = new BasicBeanBus<T>(
			new Runnable() {
				@Override
				public void run() {
					stopTheBus();
				}
			}) {
		public String toString() {
			return BasicBeanBus.class.getSimpleName() + " for " + 
					AbstractBusComponent.this.toString();
		}
	};
	
	protected void startBus() throws BusCrashException {
		logger.debug("Starting Bus.");
		beanBus.startBus();
	}
	
	protected void accept(T bean) throws BusCrashException {
		beanBus.accept(bean);
	}
	
	protected void stopBus() throws BusCrashException {
		logger.debug("Stopping Bus.");
		beanBus.stopBus();
	}

	protected void requestBusStop() {
		beanBus.requestBusStop();
	}
	
	/**
	 * Implementation override this to perform the action of 
	 * stopping the bus.
	 */
	protected abstract void stopTheBus();
	
	@Override
	public BusService getServices() {
		return new BusService(beanBus);
	}
		
	/**
	 * Set the destination.
	 * 
	 * @param to
	 */
	public void setTo(Collection<? super T> to) {
		beanBus.setTo(to);
	}
	
	public Collection<? super T> getTo() {
		return beanBus.getTo();
	}
	
}
