package org.oddjob.structural;

import org.oddjob.OddjobException;

/**
 * An exception which can be thrown by a Strutured node
 * which is cascading a child exception upwards.
 * <p>
 * This has no cause as this 'cause' as this causes unnecessary
 * stack trace in logs. The child exception is available via
 * the getChildException method if required 
 * 
 * @author Rob Gordon
 */

public class OddjobChildException extends OddjobException {
	private static final long serialVersionUID = 20070424;
	
	private final Throwable childException;
	
	private final String childName;
	

	/**
	 * Constructs a new oddjob excpetion with the given message an cause.
	 * 
	 * @param childException The child Exception.
	 * @param childName The childs name.
	 */
	public OddjobChildException(Throwable childException, String childName) {
		super("Exception in Child Job [" + childName + "]");
		this.childException = childException;
		this.childName = childName;
	}


	public Throwable getChildException() {
		return childException;
	}


	public String getChildName() {
		return childName;
	}

} 
