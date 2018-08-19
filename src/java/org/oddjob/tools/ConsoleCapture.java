package org.oddjob.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.oddjob.OddjobConsole;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogListener;
import org.slf4j.Logger;

/**
 * Capturing Oddjob.CONSOLE: Because tests append to the console log to 
 * avoid capturing test logging then the Logger class must be Loaded before 
 * the Oddjob class is loaded. This is because Oddjob's static initialiser
 * replaces the default stdout with it's own console capture. If the Logger
 * class loads first it will be appending to original stdout, not the
 * new stdout.
 * <p>
 * This is most easily achieved by including a logger in the test.
 * 
 * @author rob
 *
 */
public class ConsoleCapture {

	static final Pattern LOG_PATTERN = Pattern.compile("^(TRACE|DEBUG| INFO| WARN|ERROR).*", Pattern.DOTALL);
	
	private int dumped;
	
	private int logged;
	
	private boolean leaveLogging;
	
	static class Console implements LogListener  {
		List<String> lines = new ArrayList<String>();

		public synchronized void logEvent(LogEvent logEvent) {
			String line = logEvent.getMessage();
			// Trim would remove leading spaces, we only want to remove end new lines.
			line = line.replaceAll("\r?\n?$", "");
			lines.add(line);
		}
	}

	private final Console console = new Console();
	
	public Close captureConsole() {
		final OddjobConsole.Close oddjobConsoleClose = OddjobConsole.initialise();
		
		final Close consoleArchiveClose = capture(OddjobConsole.console());
		
		return new Close() {
			@Override
			public void close() {
				consoleArchiveClose.close();
				oddjobConsoleClose.close();
			}
		};
	}
	
	public Close capture(LogArchive archive) {

		Predicate<String> filter = getFilter();
		
		LogListener logListener = new LogListener() {
		
				@Override
				public void logEvent(LogEvent logEvent) {
					String message = logEvent.getMessage();
					if (filter.test(message)) {
						console.logEvent(logEvent);
					}
				}
			};
		
		archive.addListener(logListener, LogLevel.INFO, -1, 0);
		
		return new Close() {
			@Override
			public void close() {
				archive.removeListener(logListener);
			}
		};
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
	
	public boolean isLeaveLogging() {
		return leaveLogging;
	}
	
	public void setLeaveLogging(boolean leaveLogging) {
		this.leaveLogging = leaveLogging;
	}
	
	public void dump() {
		System.out.println("******************");
		for (; dumped < console.lines.size(); ++dumped) {
			System.out.println(console.lines.get(dumped));
		}
		System.out.println("******************");
	}
	
	public void dump(Logger logger) {
		logger.info("******************");
		for (; logged < console.lines.size(); ++logged) {
			logger.info(console.lines.get(logged));
		}
		logger.info("******************");
	}

	public static interface Close extends AutoCloseable {
		
		public void close();
	}

	public Predicate<String> getFilter() {
		if (leaveLogging) {
			return s -> true;
		}
		else {
			return s -> !LOG_PATTERN.matcher(s).matches();
		}
	}
}
