package org.oddjob;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;

/**
 * Note that because tests append to the console log to use this class
 * the logging must be configured before this class loads. The is most
 * easily achieved by including a logger in the test.
 * 
 * @author rob
 *
 */
public class ConsoleCapture {

	private int dumped;
	
	private int logged;
	
	class Console implements LogListener  {
		List<String> lines = new ArrayList<String>();
		
		public synchronized void logEvent(LogEvent logEvent) {
			lines.add(logEvent.getMessage());
		}
	}

	private final Console console = new Console();
	
	private LogArchive archive;
	
	public void capture(LogArchive archive) {
		if (this.archive != null) {
			throw new IllegalStateException("Already listening to " + archive);
		}

		this.archive = archive;
		archive.addListener(console, LogLevel.INFO, -1, 0);
	}

	public void close() {
		if (archive != null) {
			archive.removeListener(console);
		}
		archive = null;
	}
	
	public String[] getLines() {
		return console.lines.toArray(new String[console.lines.size()]);
	}
	
	public String getAll() {
		StringBuilder builder = new StringBuilder();
		for (String line : console.lines) {
			builder.append(line);
		}
		return builder.toString();
	}
	
	public int size() {
		return console.lines.size();
	}
	
	public void dump() {
		System.out.println("******************");
		for (; dumped < console.lines.size(); ++dumped) {
			System.out.print(console.lines.get(dumped));
		}
		System.out.println("******************");
	}
	
	public void dump(Logger logger) {
		logger.info("******************");
		for (; logged < console.lines.size(); ++logged) {
			logger.info(console.lines.get(logged).replaceFirst("\r?\n?$", ""));
		}
		logger.info("******************");
	}
}
