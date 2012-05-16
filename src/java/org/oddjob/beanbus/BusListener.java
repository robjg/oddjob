package org.oddjob.beanbus;

import java.util.EventListener;

/**
 * Something that listens to {@link BeanBus} events broadcast by 
 * an {@link BusNotifier}.
 * 
 * @author rob
 *
 */
public interface BusListener extends EventListener {

	/**
	 * The bus is starting. Called before any beans have gone anywhere.
	 *  
	 * @param event
	 * @throws CrashBusException
	 */
	void busStarting(BusEvent event) throws CrashBusException;
		
	/**
	 * The bus is stopping. Called when all the beans have arrived at 
	 * their destination. This notification will only be received if
	 * the bus hasn't crashed. The bus can crash while stopping.
	 * 
	 * @param event
	 * @throws CrashBusException
	 */
	void busStopping(BusEvent event) throws CrashBusException;
	
	/**
	 * The bus has terminated. Called after either a <code>busStopping</code>
	 * or <code>busCrashed</code> notification. Always called.
	 * 
	 * @param event
	 */
	void busTerminated(BusEvent event);
	
	/**
	 * Called if the bus has crashed. The bus can crash while starting, 
	 * stopping and any time in between.
	 * 
	 * @param event
	 * @param e The exception thrown by the bus crashing.
	 */
	void busCrashed(BusEvent event, BusException e);
}
