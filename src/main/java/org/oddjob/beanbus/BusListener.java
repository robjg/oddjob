package org.oddjob.beanbus;

import java.util.EventListener;

/**
 * Something that listens to {@link BeanBus} events broadcast by 
 * an {@link BusConductor}.
 * 
 * @author rob
 *
 */
public interface BusListener extends EventListener {

	/**
	 * The bus is starting. Called before any beans have gone anywhere.
	 *  
	 * @param event
	 * @throws BusCrashException
	 */
	void busStarting(BusEvent event) throws BusCrashException;
		
	/**
	 * Stage starting.
	 * 
	 * @param event
	 */
	public void tripBeginning(BusEvent event) throws BusCrashException;
	
	/**
	 * Stage complete.
	 * 
	 * @param event
	 */
	public void tripEnding(BusEvent event) throws BusCrashException;
	
	/**
	 * The bus is stopping. Called when all the beans have arrived at 
	 * their destination. This notification will only be received if
	 * the bus hasn't crashed. The bus can crash while stopping.
	 * 
	 * @param event
	 * @throws BusCrashException
	 */
	void busStopping(BusEvent event) throws BusCrashException;
	
	/**
	 * 
	 * @param event
	 */
	void busStopRequested(BusEvent event);
	
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
	 */
	void busCrashed(BusEvent event);
	
}
