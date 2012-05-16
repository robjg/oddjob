package org.oddjob.beanbus;

/**
 * Something that notifies anything that wants to know about events
 * happening to the bus.
 * 
 * @author rob
 *
 */
public interface BusNotifier {

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
