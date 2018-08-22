package org.oddjob.jobs.tasks;

/**
 * An exception to be used by Tasks.
 * 
 * @author Rob Gordon
 */

public class TaskException extends Exception {
	private static final long serialVersionUID = 2015050600L;
	
	/**
	 * Constructs a new Exception with no message an no cause.
	 *  
	 */
	public TaskException() {
		
		super();
	}

	/**
	 * Constructs a new Exception with the given message an cause.
	 * 
	 * @param s The message.
	 * @param t The cause.
	 */
	public TaskException(String s, Throwable t) {
		
		super(s, t);
	}

	/**
	 * Constructs a new Exception with the given cause.
	 * 
	 * @param t The cause.
	 */
	public TaskException(Throwable t) {
		
		super(t);
	}

	/**
	 * Constructs a new Exception with given message.
	 * 
	 * @param s The message.
	 */
	public TaskException(String s) {
		
		super(s);
	}
} 
