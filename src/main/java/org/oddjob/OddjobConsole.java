package org.oddjob;

import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LoggingPrintStream;
import org.oddjob.logging.cache.LogArchiveImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Manage capture of console output to ensure original stream is replaced.
 * <p>
 * Oddjob used to set the console capture once but this didn't play well
 * with Ant JUnit running in fork once mode as that replaces stdout and stderr
 * at the beginning of each test.
 * 
 * @since 1.5
 * 
 * @author rob
 *
 */
public class OddjobConsole {

	private static final Logger logger = LoggerFactory.getLogger(OddjobConsole.class);

	private static final Deque<Console> stack = new LinkedList<>();
	
	public static Close initialise() {
		
		synchronized (stack) {
			
			if (!stack.isEmpty()) {
				Console current = stack.peek();
				if (current.stdoutLoggingPrintStream == System.out && 
						current.stderrLoggingPrintStream == System.err) {
							
					return () -> {
						// Nothing to restore
					};
				}
			}
		
		    final PrintStream originalStdOut = System.out;
		    final PrintStream originalStdErr = System.err;
	
	    	LogArchiveImpl archive = new LogArchiveImpl("CONSOLE_MAIN", LogArchiver.MAX_HISTORY);

			final int consoleCount = stack.size();

	    	PrintStream stdoutLoggingPrintStream = new LoggingPrintStream(System.out, LogLevel.INFO, archive) {
				@Override
				public String toString() {
					return "OddjobConsoleStdOut_" + consoleCount;
				}
			};
	    	PrintStream stderrLoggingPrintStream = new LoggingPrintStream(System.err, LogLevel.ERROR, archive) {
				@Override
				public String toString() {
					return "OddjobConsoleStdErr_" + consoleCount;
				}
			};

			// Force logger class to load first so that console appender attaches
			// to original not to ours.
			logger.debug("Replacing stderr [{}] with [{}] and stdout [{}] with [{}].",
					originalStdErr, stderrLoggingPrintStream, originalStdOut, stdoutLoggingPrintStream);

			Console console = new Console(archive, stdoutLoggingPrintStream, stderrLoggingPrintStream);

			synchronized (stack) {
				stack.push(console);

				System.setOut(stdoutLoggingPrintStream);
				System.setErr(stderrLoggingPrintStream);
			}

	    	return () -> {

				synchronized (stack) {

					Console current = stack.pop();

					if (current != console) {
						throw new IllegalStateException("Not current console");
					}

					if (System.out == stdoutLoggingPrintStream) {

						logger.debug("Restoring stdout from [{}] to [{}].",
								System.out, originalStdOut);
					} else {

						logger.debug("Something has set stdout to [{}] - it will be restored with [{}]",
								System.out, originalStdOut);
					}

					if (System.err == stderrLoggingPrintStream) {

						logger.debug("Restoring stderr [{}] to [{}]",
								System.err, originalStdErr);
					} else {

						logger.debug("Something has set stderr [{}] - it will be restored with [{}]",
								System.err, originalStdErr);
					}

					System.out.flush();
					System.err.flush();

					System.setOut(originalStdOut);
					System.setErr(originalStdErr);
				}
			};
		}		
	}
	
	public static LogArchive console() {
		synchronized (stack) {
			if (stack.isEmpty()) {
				throw new IllegalStateException("OddjobConsole not initialised.");
			}
			return stack.peek().console;
		}
	}
	
	public interface Close extends AutoCloseable {
		
		@Override
		void close();
	}
	
	static class Console {
		
	    /** The archiver to which all console output will be captured. */
	    private final LogArchive console;
	    
	    private final PrintStream stdoutLoggingPrintStream;
	    
	    private final PrintStream stderrLoggingPrintStream;
	    
	    Console(LogArchive console, PrintStream stdoutLoggingPrintStream, PrintStream stderrLoggingPrintStream) {
	    	this.console = console;
	    	this.stdoutLoggingPrintStream = stdoutLoggingPrintStream;
	    	this.stderrLoggingPrintStream = stderrLoggingPrintStream;
	    }
	}
}
