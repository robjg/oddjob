package org.oddjob;

/**
 * A class that implements this interface is able to respond
 * to two types of reset message. A soft reset, which is typically
 * used to reset a job after an exception, or a hard reset which is
 * typically used to reset a job to begin again.
 * 
 * @author Rob Gordon
 */
public interface Resettable {
	
	/**
	 * Perform a soft reset.
	 * 
	 * @return true if successful.
	 */
	boolean softReset();
	
	/**
	 * Perform a hard reset.
	 * 
	 * @return true if successful.
	 */
	boolean hardReset();
}
