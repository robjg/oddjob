package org.oddjob.framework;

/**
 * Thrown when an action is attempted on a job that has
 * been destroyed.
 * 
 * @author rob
 */
public class JobDestroyedException extends RuntimeException {
	private static final long serialVersionUID = 2009091000L;

	/**
	 * Creates the exception for the destroyed job.
	 * 
	 * @param job The job 
	 */
	public JobDestroyedException(Object job) {
		super("Job Destroyed: " + job.toString());
	}
}
