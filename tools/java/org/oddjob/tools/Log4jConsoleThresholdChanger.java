package org.oddjob.tools;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class Log4jConsoleThresholdChanger implements AutoCloseable {

	private final Map<ConsoleAppender, Priority> consoleAppenders = new HashMap<>();
	
	public Log4jConsoleThresholdChanger() {
		this(Level.WARN);
	}
	
	public Log4jConsoleThresholdChanger(Level newLevel) {
		
		Logger logger = Logger.getRootLogger();
		
		@SuppressWarnings("unchecked")
		Enumeration<Appender> appenders = logger.getAllAppenders();
		
		while (appenders.hasMoreElements()) {
			Appender appender = appenders.nextElement();
			
			if (appender instanceof ConsoleAppender) {
				ConsoleAppender consoleAppender = (ConsoleAppender) appender;
				
				Priority existingLevel = consoleAppender.getThreshold();
				consoleAppenders.put(consoleAppender, existingLevel);
				consoleAppender.setThreshold(newLevel);
			}
		}
		
	}
	
	@Override
	public void close() {
		
		for (Map.Entry<ConsoleAppender, Priority> entry : consoleAppenders.entrySet()) {
			
			entry.getKey().setThreshold(entry.getValue());
		}
	}
	
}
