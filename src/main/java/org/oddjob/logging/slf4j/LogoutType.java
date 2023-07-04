package org.oddjob.logging.slf4j;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @oddjob.description Provide an output to a logger. With a default
 * Oddjob configuration log messages will be visible in the Log panel
 * of Oddjob Explorer.
 * 
 * @oddjob.example
 * 
 * Copy the contents of a file to the logger.
 * 
 * {@oddjob.xml.resource org/oddjob/logging/slf4j/LogoutExample.xml}
 * 
 */
public class LogoutType implements ArooaValue {

	public static class Conversions implements ConversionProvider {

		@Override
		public void registerWith(ConversionRegistry registry) {

			registry.register(LogoutType.class, OutputStream.class,
					LogoutType::toOutputStream);

			registry.register(LogoutType.class, Consumer.class,
					LogoutType::toConsumer);
		}
	}


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
	
	public OutputStream toOutputStream() throws ArooaConversionException {

		return new Slf4jPrintStream(logger(), level);
	}

	public Consumer<Object> toConsumer() {

		Logger logger = logger();

		LogLevel level = Optional.ofNullable(this.level).orElse(LogLevel.INFO);

		switch (level) {
			case TRACE:
				return o -> logger.trace(Objects.toString(o));
			case DEBUG:
				return o -> logger.debug(Objects.toString(o));
			case INFO:
				return o -> logger.info(Objects.toString(o));
			case WARN:
				return o -> logger.warn(Objects.toString(o));
			case ERROR:
			case FATAL:
				return o -> logger.error(Objects.toString(o));
			default:
				throw new RuntimeException("Unexpected Level " + level);
		}
	}

	private Logger logger() {

		String logName = this.logger;
		if (logName == null) {
			logName = LogoutType.class.getName();
		}
		return LoggerFactory.getLogger(logName);
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
