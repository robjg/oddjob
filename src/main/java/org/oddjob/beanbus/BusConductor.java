package org.oddjob.beanbus;

import org.oddjob.beanbus.destinations.Batcher;

import java.io.Flushable;

/**
 * Provides co-ordination for a Bean Bus.
 * <p>
 * 
 * @author rob
 *
 */
public interface BusConductor extends Flushable, AutoCloseable {

	/**
	 * Cleaning the bus will cause the trip to end and a new one to 
	 * begin. Intended for components such as {@link Batcher} so that
	 * they can flush the bus.
	 * 
	 */
	@Override
	void flush();

	/**
	 * Request that the bus stop.
	 */
	@Override
	void close();
}
