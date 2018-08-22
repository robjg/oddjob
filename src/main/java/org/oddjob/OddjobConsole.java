package org.oddjob;

import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;

import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogEventSink;
import org.oddjob.logging.LoggingPrintStream;
import org.oddjob.logging.cache.LogArchiveImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
						current.stderrloggingPrintStream == System.err) {
							
					return new Close() {
						@Override
						public void close() {
							// Nothing to restore
						}
					};
				}
			}
		
		    final PrintStream originalStdOut = System.out;
		    final PrintStream originalStdErr = System.err;
	
	    	// Force logger class to load first so that console appender attaches
	    	// to original not to ours.
	    	logger.debug("Replacing sdterr [{}] and stdout [{}].", originalStdErr, originalStdOut);
	    	
	    	LogArchive archive = new LogArchiveImpl("CONSOLE_MAIN", LogArchiver.MAX_HISTORY);
	    	
	    	PrintStream stdoutLoggingPrintStream = new LoggingPrintStream(System.out, LogLevel.INFO, 
	    			(LogEventSink) archive);
	    	PrintStream stderrloggingPrintStream = new LoggingPrintStream(System.err, LogLevel.ERROR, 
	    			(LogEventSink) archive);
	
	    	Console console = new Console(archive, stdoutLoggingPrintStream, stderrloggingPrintStream);

	    	stack.push(console);
	    	
	    	System.setOut(stdoutLoggingPrintStream);
	    	System.setErr(stderrloggingPrintStream);	    	
		
	    	return new Close() {
				
				@Override
				public void close() {
					
					synchronized (stack) {
					
						Console current = stack.pop();

						if (current != console) {
							throw new IllegalStateException("Not current console");
						}
						
						if (System.out != stdoutLoggingPrintStream) {
							
							logger.debug("Something has set stdout to [{}] - it will be replaced!", System.out);
						}
						if (System.err != stderrloggingPrintStream) {
							
							logger.debug("Something has set stderr [{}] - it will be replaced!", System.err);
						}

						System.out.flush();
						System.err.flush();
							
				    	System.setOut(originalStdOut);
				    	System.setErr(originalStdErr);
					}
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
		public void close();
	}
	
	static class Console {
		
	    /** The archiver to which all console output will be captured. */
	    private final LogArchive console;
	    
	    private final PrintStream stdoutLoggingPrintStream;
	    
	    private final PrintStream stderrloggingPrintStream;
	    
	    Console(LogArchive console, PrintStream stdoutLoggingPrintStream, PrintStream stderrloggingPrintStream) {
	    	this.console = console;
	    	this.stdoutLoggingPrintStream = stdoutLoggingPrintStream;
	    	this.stderrloggingPrintStream = stderrloggingPrintStream;
	    }
	    
	    
	}
}
