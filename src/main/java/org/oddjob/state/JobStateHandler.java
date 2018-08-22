package org.oddjob.state;

import org.oddjob.Stateful;


/**
 * Helps Jobs handle state change.
 * 
 * @author Rob Gordon
 */

public class JobStateHandler 
extends StateHandler<JobState> {
	
	/**
	 * Constructor.
	 * 
	 * @param source The source for events.
	 */
	public JobStateHandler(Stateful source) {
		super(source, JobState.READY);
	}	
	
}

