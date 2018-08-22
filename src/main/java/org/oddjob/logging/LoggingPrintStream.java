/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;

import java.io.OutputStream;
import java.io.PrintStream;

import org.oddjob.arooa.logging.LogLevel;

/**
 * Provide a PrintStream that logs to a logger.
 * 
 * @author Rob Gordon.
 */
public class LoggingPrintStream extends PrintStream {
	
	public LoggingPrintStream(OutputStream existing, LogLevel level, 
			LogEventSink consoleArchiver) {
		super(new LoggingOutputStream(existing, level, consoleArchiver));
	}
}
