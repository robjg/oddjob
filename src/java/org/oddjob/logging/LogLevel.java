package org.oddjob.logging;



/**
 * An Oddjob idea of a Log Level. Try to reduce coupling with Log4j.
 * 
 * @author Rob Gordon
 */
public enum LogLevel {
	
	/**
	 * The finest.
	 */	
	TRACE,
	
	/**
	 * For debugs.
	 */
	DEBUG,
	
	/**
	 * Stuff the user should see.
	 */
	INFO,

	/**
	 * Warnings.
	 */
	WARN, 

	/**
	 * Logged to the console.
	 */
	ERROR,
	
	/**
	 * Process shouldn't be able to continue.
	 */
	FATAL,
	
	;

	public boolean isLessThan(LogLevel other) {
		return this.ordinal() < other.ordinal();
	}
	
}

