package org.oddjob.beanbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * A base class for Jobs and Services that provide an {@link BeanBus}.
 * <p>
 * Implementations must ensure {@link #startBus() and {@link #stopBus()}
 * are called and must provide a {@link #stopTheBus()} method.
 * 
 * 
 * @author rob
 *
 * @param <T> The type of beans on the bus.
 */
abstract public class AbstractBusComponent<T> 
implements BusServiceProvider, Outbound<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractBusComponent.class);
	
	private final BasicBeanBus<T> beanBus = new BasicBeanBus<T>(
			this::stopTheBus) {
		public String toString() {
			return BasicBeanBus.class.getSimpleName() + " for " +
					AbstractBusComponent.this;
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
		beanBus.getBusConductor().close();
	}
	
	/**
	 * Implementation override this to perform the action of 
	 * stopping the bus.
	 */
	protected abstract void stopTheBus();
	
	@Override
	public SimpleBusService getServices() {
		return new SimpleBusService(beanBus.getBusConductor());
	}
		
	/**
	 * Set the destination.
	 * 
	 * @param to
	 */
	@Override
	public void setTo(Consumer<? super T> to) {
		beanBus.setTo(to);
	}
	
	public Consumer<? super T> getTo() {
		return beanBus.getTo();
	}
	
}
