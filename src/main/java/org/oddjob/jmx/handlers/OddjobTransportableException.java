package org.oddjob.jmx.handlers;

/**
 * An exception that can be sent across the wire without the class that
 * caused the exception being available at the client. This exception
 * still carries the message and all the stack trace information from the 
 * original exception so a problem can be diagnosed.
 * 
 * @author rob
 *
 */
public class OddjobTransportableException extends Exception {
	private static final long serialVersionUID = 2012032200L; 

	private final String originalExcpetionClassName;
	
	public OddjobTransportableException(Throwable t) {
		super(t.getMessage());
		this.originalExcpetionClassName = t.getClass().getName();
		setStackTrace(t.getStackTrace());
		if (t.getCause() != null) {
			initCause(new OddjobTransportableException(t.getCause()));
		}
	}
	
	public String getOriginalExcpetionClassName() {
		return originalExcpetionClassName;
	}
	
	@Override
	public String toString() {
        String message = getMessage();
        return (message != null) ? 
        		(originalExcpetionClassName + ": " + message) : 
        			originalExcpetionClassName;
	}	
}
