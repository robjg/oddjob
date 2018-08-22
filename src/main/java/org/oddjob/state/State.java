package org.oddjob.state;

import java.io.Serializable;

import org.oddjob.Stateful;

/**
 * The state of a {@link Stateful} job. Oddjob uses the set of 
 * conditions of a state to decide what actions to allow and also
 * what state to reflect up the hierarchy.
 * 
 * @author rob
 *
 */
public interface State extends Serializable {

	/**
	 * Is a job ready to be executed.
	 * 
	 * @return true/false.
	 */
	public boolean isReady();
	
	/**
	 * Is the job executing. This is normally when the thread of
	 * execution is still within the job.
	 * 
	 * @return true/false.
	 */
	public boolean isExecuting();
	
	/**
	 * Can a job be stopped? This is a catch all for jobs
	 * that are active or executing.
	 * 
	 * @return true/false.
	 */
	public boolean isStoppable();
		
	/**
	 * Is a job or service complete?
	 * 
	 * @return true/false.
	 */
	public boolean isComplete();
 	
	/**
	 * Is a job or service incomplete. The implication of incomplete is
	 * that it could be retried to be complete at some future date.
	 * 
	 * @return true/false.
	 */
	public boolean isIncomplete();
	
	/**
	 * Is a job in an exception state. This is generally due to an 
	 * unexpected error, as opposed to incomplete which in some way 
	 * is expected.
	 * 
	 * @return true/false.
	 */
	public boolean isException();
	
	
	/**
	 * The job is destroyed. It is no longer available for anything.
	 * 
	 * @return true/false.
	 */
	public boolean isDestroyed();
}
