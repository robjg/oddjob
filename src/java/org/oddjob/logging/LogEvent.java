/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;

import java.io.Serializable;

/**
 * A LogEvent is archived in a LogArchiver. These LogEvents are Serialised so
 * they may be sent across the network. 
 * <p>
 * The log message should already be formatted. 
 * <p>
 * The sequence number allows a local LogArchive to synchronise with 
 * a remote LogArchive.
 */
public class LogEvent implements Serializable {
	private static final long serialVersionUID = 20061214;
	
	private final long number;
	private final LogLevel level;
	private final String logger;
	private final String message;
	
	/**
	 * Construct a new LogEvent.
	 * 
	 * @param logger The logger (or archive). Must not be null.
	 * @param number The sequence number for this event.
	 * @param level The LogLevel. Must not be null.
	 * @param message The formatted message. Must not be null.
	 */
	public LogEvent(String logger, 
			long number, LogLevel level, 
			String message) {
		if (logger == null) {
			throw new NullPointerException("Logger can not be null!");
		}
		if (number < 0) {
			throw new IllegalArgumentException("Sequence Number must be positive!");
		}
		if (level == null) {
			throw new NullPointerException("Log Level can not be null!");
		}
		if (message == null) {
			throw new NullPointerException("Message can not be null!");
		}
		
		this.number = number;
		this.level = level;
		this.logger = logger;
		this.message = message;
	}
	/**
	 * @return Returns the level.
	 */
	public LogLevel getLevel() {
		return level;
	}
	/**
	 * @return Returns the logger.
	 */
	public String getLogger() {
		return logger;
	}
	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @return Returns the number.
	 */
	public long getNumber() {
		return number;
	}
}
