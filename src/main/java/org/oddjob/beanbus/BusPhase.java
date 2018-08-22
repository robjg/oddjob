package org.oddjob.beanbus;

/**
 * Phases of a Bus Journey. Intended to provide more information to 
 * {@link BusEvent}s, not for state logic.
 * 
 * @author rob
 *
 */
public enum BusPhase {

	/**
	 * The bus isn't running.
	 */
	BUS_STOPPED,
	
	/**
	 * The bus is starting.
	 */
	BUS_STARTING,
	
	/**
	 * A trip is beginning.
	 */
	TRIP_BEGINNING,
	
	/**
	 * The bus is running.
	 */
	BUS_RUNNING,
	
	/**
	 * A trip is intending.
	 */
	TRIP_ENDING,
	
	/**
	 * The bus is stopping.
	 */
	BUS_STOPPING,
	
	/**
	 * The bus crashed.
	 */
	BUS_CRASHED,
}
