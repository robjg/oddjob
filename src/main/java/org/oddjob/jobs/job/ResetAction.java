package org.oddjob.jobs.job;

/**
 * Something that can apply a reset to a component.
 * 
 * @see ResetActions
 * @see ResetJob
 * 
 * @author rob
 *
 */
public interface ResetAction {
	
	/**
	 * Apply the reset.
	 * 
	 * @param job The job being reset.
	 */
	void doWith(Object job);
}