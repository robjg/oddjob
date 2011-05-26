package org.oddjob.logging.log4j;

import java.io.OutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.oddjob.logging.AbstractLoggingOutput;

/**
 * An {@code OutputStream} that write it's output to a Log4j logger.
 * 
 * @author rob
 *
 */
public class Log4jPrintStream extends AbstractLoggingOutput {

	private final Logger logger;
	
	private final Priority priority;
	
	/**
	 * Constructor.
	 * 
	 * @param logger The log4j Logger.
	 * @param level The Log4j level.
	 */
	public Log4jPrintStream(
			Logger logger, Level level) {
		this(null, logger, level);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param existing An optional existing stream output will be 'teed' to.
	 * @param logger The log4j Logger.
	 * @param level The Log4j level.
	 */
	public Log4jPrintStream(OutputStream existing, 
			Logger logger, Level level) {
		super(existing);
		this.logger = logger;
		this.priority = level;
	}
	
	@Override
	protected void dispatch(String message) {
		logger.log(priority, message.trim());
	}
}
