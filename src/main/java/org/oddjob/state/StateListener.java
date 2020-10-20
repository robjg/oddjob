package org.oddjob.state;



/**
 * Implementors of this interface are able to listen to state events.
 * 
 * @author Rob Gordon
 */

@FunctionalInterface
public interface StateListener {


	/**
	 * Triggered when the job state changes.
	 * 
	 * @param event The job state event.
	 */
	void jobStateChange(StateEvent event);
	
}
