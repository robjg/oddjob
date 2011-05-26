package org.oddjob.state;



/**
 * Encapsulate the allowed states for a job.
 * 
 * @author Rob Gordon
 */

public enum JobState {
	
	/**
	 * Indicates the job is ready to be executing. The Oddjob Framework
	 * will only execute a job which is in this state.
	 */	
	READY,

	/**
	 * Indicates the job is executing.
	 */	
	EXECUTING,

	/**
	 * Indicates the job is not complete. Typically this is not unexpected,
	 * for instance a job which looks for a file, and a
	 * parent job will re-execute the job again at a later date. 
	 */
	INCOMPLETE,
		
	/**
	 * Indicates job has completed. 
	 */	
	COMPLETE,
	
	/**
	 * Indicates an exception has occurred. This is generally 
	 * recoverable. Such as database failure, disk full etc.
	 */	
	EXCEPTION,
	
	/**
	 * The job has been destroyed. It can no longer be used.
	 */	
	DESTROYED,
	;
	
	/**
	 * Utility function to convert a state text to to the JobState.
	 * 
	 * @param state Case insensitive text.
	 * @return The corresponding jobState or null if it's invalid.
	 */
	public static JobState stateFor(String state) {
		state = state.toUpperCase();
		return valueOf(state);
	}
}

