package org.oddjob;

import org.oddjob.framework.JobDestroyedException;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;


/**
 * A Stateful job implements this interface so that 
 * it's state can be discovered. State is used to 
 * control the flow of execution within Oddjob, as
 * well as being a way of informing client applications
 * of progress.
 * 
 * @author Rob Gordon
 */
public interface Stateful {

	/**
	 * Add a job state listener.
	 * 
	 * @param listener The listener.
	 * 
	 * @throw JobDestroyedException If state is already destroyed.
	 */
	void addStateListener(StateListener listener)
	throws JobDestroyedException;

	/**
	 * Remove a job state listener.
	 * 
	 * @param listener The listener.
	 */
	void removeStateListener(StateListener listener);

	/**
	 * Get the last state event.
	 * 
	 * @return The last State Event
	 */
	StateEvent lastStateEvent();
	
}
