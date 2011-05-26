package org.oddjob.logging.log4j;

import java.io.OutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;

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
	 * @oddjob.description The log log4j log level.
	 * @oddjob.required No. Defaults to INFO.
	 */
	private String level;
	
	@Override
	public OutputStream toValue() throws ArooaConversionException {
		
		String logName = this.logger;
		if (logName == null) {
			logName = LogoutType.class.getName();
		}
		Logger logger = Logger.getLogger(logName);
		
		Level level = Level.toLevel(
				this.level == null ? "INFO" : this.level);
		
		return new Log4jPrintStream(logger, level);
	}

	public String getLogger() {
		return logger;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
}
