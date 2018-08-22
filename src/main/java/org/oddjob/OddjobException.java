package org.oddjob;

/**
 * An exception to be used by Oddjob jobs.
 * 
 * @author Rob Gordon
 */

public class OddjobException extends RuntimeException {
	private static final long serialVersionUID = 2009091100L;
	
	/**
	 * Constructs a new oddjob exception with no message an no cause.
	 *  
	 */
	public OddjobException() {
		
		super();
	}

	/**
	 * Constructs a new oddjob excpetion with the given message an cause.
	 * 
	 * @param s The message.
	 * @param t The cause.
	 */
	public OddjobException(String s, Throwable t) {
		
		super(s, t);
	}

	/**
	 * Constructs a new Oddjob exception with the given cause.
	 * 
	 * @param t The cause.
	 */
	public OddjobException(Throwable t) {
		
		super(t);
	}

	/**
	 * Constructs a new Oddjob exception with given message.
	 * 
	 * @param s The message.
	 */
	public OddjobException(String s) {
		
		super(s);
	}
} 
