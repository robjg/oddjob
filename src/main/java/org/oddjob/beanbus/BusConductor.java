package org.oddjob.beanbus;

import org.oddjob.beanbus.destinations.Batcher;

import java.io.Flushable;

/**
 * Provides co-ordination for a {@link BeanBus}.
 * <p>
 * 
 * @author rob
 *
 */
public interface BusConductor extends Flushable {

	/**
	 * Cleaning the bus will cause the trip to end and a new one to 
	 * begin. Intended for components such as {@link Batcher} so that
	 * they can flush the bus.
	 * 
	 */
	@Override
	void flush();
	
	/**
	 * Request that the bus stop. This may, and probably will,
	 * be called asynchronously.
	 */
	void requestBusStop();
	
	/**
	 * Add a listener.
	 * 
	 * @param listener The listener.
	 */
	void addBusListener(BusListener listener);
	
	/**
	 * Remove the listener.
	 * 
	 * @param listener The listener.
	 */
	void removeBusListener(BusListener listener);
}
