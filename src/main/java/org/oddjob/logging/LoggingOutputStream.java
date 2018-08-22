package org.oddjob.logging;

import java.io.OutputStream;

import org.oddjob.arooa.logging.LogLevel;

/**
 * An output stream that splits output into an existing
 * output stream if supplied, and a console archive. 
 * <p>
 *
 */
public class LoggingOutputStream extends AbstractLoggingOutput {
	
	private final LogLevel level;
	
	private final LogEventSink consoleArchiver;
	
	/**
	 * Constructor.
	 * 
	 * @param existing The output stream to also write to. May be null.
	 * @param level The level to use when logging.
	 * @param consoleArchiver The logger to write to.
	 */
	public LoggingOutputStream(OutputStream existing, LogLevel level, 
			LogEventSink consoleArchiver) {
		super(existing);
		this.level = level;
		this.consoleArchiver = consoleArchiver;
	}
	
	@Override
	protected void dispatch(String message) {
		consoleArchiver.addEvent(level, message);
	}
}