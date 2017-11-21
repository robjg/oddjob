package org.oddjob.logging.slf4j;

import java.io.OutputStream;
import java.util.Optional;

import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.AbstractLoggingOutput;
import org.slf4j.Logger;

/**
 * An {@code OutputStream} that write it's output to an  Slf4J logger.
 * 
 * @author rob
 *
 */
public class Slf4jPrintStream extends AbstractLoggingOutput {

	private final Logger logger;
	
	private final LogLevel level;
	
	/**
	 * Constructor.
	 * 
	 * @param logger The SLF4J Logger.
	 * 
	 */
	public Slf4jPrintStream(
			Logger logger) {
		this(null, logger, null);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param logger The SLF4J Logger.
	 * @param level The log level.
	 */
	public Slf4jPrintStream(
			Logger logger, LogLevel level) {
		this(null, logger, level);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param existing An optional existing stream output will be 'teed' to.
	 * @param logger The SLF4J Logger.
	 * @param level The log level.
	 */
	public Slf4jPrintStream(OutputStream existing, 
			Logger logger, LogLevel level) {
		super(existing);
		this.logger = logger;
		this.level = Optional.ofNullable(level).orElse(LogLevel.INFO);
	}
	
	@Override
	protected void dispatch(String message) {
		message = message.trim();

		switch (level) {
		case TRACE:
			logger.trace(message);
			break;
		case DEBUG:
			logger.debug(message);
			break;
		case INFO:
			logger.info(message);
			break;
		case WARN:
			logger.warn(message);
			break;
		case ERROR:
		case FATAL:
			logger.error(message);
			break;
		default:
			throw new RuntimeException("Unexpected Level " + level);
		}
	}
}
