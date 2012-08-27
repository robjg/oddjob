package org.oddjob;

import org.oddjob.framework.JobDestroyedException;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.util.OddjobLockTimeoutException;


/**
 * A stateful job implements this interface so that 
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
	 * @throws OddjobLockTimeoutException If the state lock can't be acquired 
	 * within the default timeout period.
	 * @throw JobDestroyedException If state is already destroyed.
	 */
	public void addStateListener(StateListener listener) 
	throws JobDestroyedException, OddjobLockTimeoutException;

	/**
	 * Remove a job state listener.
	 * 
	 * @param listener The listener.
	 * 
	 * @throws OddjobLockTimeoutException If the state lock can't be acquired 
	 * within the default timeout period.
	 */	
	public void removeStateListener(StateListener listener)
	throws OddjobLockTimeoutException;

	/**
	 * Get the last state event.
	 * 
	 * @return The last State Event
	 * 
	 * @throws OddjobLockTimeoutException If the state lock can't be acquired 
	 * within the default timeout period.
	 * 
	 */
	public StateEvent lastStateEvent() throws OddjobLockTimeoutException;
	
}
