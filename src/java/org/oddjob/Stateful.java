package org.oddjob;

import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;


/**
 * A stateful job implments this interface so that 
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
	 */

	public void addJobStateListener(JobStateListener listener);

	/**
	 * Remove a job state listener.
	 * 
	 * @param listener The listener.
	 */
	
	public void removeJobStateListener(JobStateListener listener);

	/**
	 * Get the last job state event.
	 * 
	 * @return
	 */
	public JobStateEvent lastJobStateEvent();
	
}
