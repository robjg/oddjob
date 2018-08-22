package org.oddjob.logging.slf4j;

import java.io.OutputStream;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.types.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @oddjob.description Provide an output to a logger. With a default
 * Oddjob configuration log messages will be visible in the Log panel
 * of Oddjob Explorer.
 * 
 * @oddjob.example
 * 
 * Copy the contents of a file to the logger.
 * 
 * {@oddjob.xml.resource org/oddjob/logging/log4j/LogoutExample.xml}
 * 
 */
public class LogoutType implements ValueFactory<OutputStream> {

	/**
	 * @oddjob.property
	 * @oddjob.description The Log4j logger name to log the output to.
	 * @oddjob.required No. Defaults to org.oddjob.logging.log4j.LogoutType
	 */
	private String logger;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The log log level.
	 * @oddjob.required No. Defaults to INFO.
	 */
	private LogLevel level;
	
	@Override
	public OutputStream toValue() throws ArooaConversionException {
		
		String logName = this.logger;
		if (logName == null) {
			logName = LogoutType.class.getName();
		}
		Logger logger = LoggerFactory.getLogger(logName);
		
		return new Slf4jPrintStream(logger, level);
	}

	public String getLogger() {
		return logger;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}

	public LogLevel getLevel() {
		return level;
	}

	public void setLevel(LogLevel level) {
		this.level = level;
	}
}
