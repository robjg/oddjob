package org.oddjob.beanbus;

import org.oddjob.beanbus.destinations.Batcher;

/**
 * Provides co-ordination for a {@link BeanBus}.
 * <p>
 * 
 * @author rob
 *
 */
public interface BusConductor {

	/**
	 * Cleaning the bus will cause the trip to end and a new one to 
	 * begin. Intended for components such as {@link Batcher} so that
	 * they can flush the bus.
	 * 
	 * @throws BusCrashException
	 */
	public void cleanBus() throws BusCrashException;
	
	/**
	 * Request that the bus stop. This may, and probably will,
	 * be called asynchronously.
	 */
	public void requestBusStop();	
	
	/**
	 * Add a listener.
	 * 
	 * @param listener The listener.
	 */
	public void addBusListener(BusListener listener);
	
	/**
	 * Remove the listener.
	 * 
	 * @param listener The listener.
	 */
	public void removeBusListener(BusListener listener);
}
