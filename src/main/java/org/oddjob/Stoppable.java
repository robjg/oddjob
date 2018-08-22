package org.oddjob;

/**
 * A class that implements this interface is able to stop executing.
 * 
 * @author Rob Gordon
 */

public interface Stoppable {
	
	/**
	 * Stop executing. This method should not return until the
	 * Stoppable has actually stopped.
	 */	
	public void stop() throws FailedToStopException;
}
