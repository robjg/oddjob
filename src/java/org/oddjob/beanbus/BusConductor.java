package org.oddjob.beanbus;

/**
 * Provides co-ordination for a {@link BeanBus}.
 * <p>
 * 
 * @author rob
 *
 */
public interface BusConductor {

	public void cleanBus() throws BusCrashException;
	
	public void requestBusStop() throws BusCrashException;	
	
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
