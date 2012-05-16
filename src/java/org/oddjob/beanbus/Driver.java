package org.oddjob.beanbus;

/**
 * The Driver is something that forces beans down a bus.
 * 
 * @author rob
 *
 * @param <T> The type of the bean on the bus.
 */
public interface Driver<T> {

	/**
	 * The destination the beans are going to.
	 * 
	 * @param to
	 */
	public void setTo(Destination<? super T> to);
	
	/**
	 * Start the driver.
	 * 
	 * @throws BusException
	 */
	public void go() throws BusException;
	
	/**
	 * Stop driving the bus.
	 */
	public void stop();
}
