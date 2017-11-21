package org.oddjob;

import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogEventSink;
import org.oddjob.logging.LoggingPrintStream;
import org.oddjob.logging.cache.LogArchiveImpl;

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

	private static final Logger logger = Logger.getLogger(OddjobConsole.class);

    /** The archiver to which all console output will be captured. */
    private static volatile LogArchive console;
    
    private static volatile PrintStream stdoutLoggingPrintStream;
    
    private static volatile PrintStream stderrloggingPrintStream;
    
	public static Close initialise() {
		
		if (System.out == stdoutLoggingPrintStream) {
			return new Close() {
				@Override
				public void close() {
					// Nothing to restore
				}
			};
		}

	    final PrintStream originalStdOut = System.out;
	    final PrintStream originalStdErr = System.err;

    	// Force logger class to load first so that console appender attaches
    	// to original not to ours.
    	logger.debug("Replacing sdterr and stdout.");
    	
    	console = new LogArchiveImpl("CONSOLE_MAIN", LogArchiver.MAX_HISTORY);
    	
    	stdoutLoggingPrintStream = new LoggingPrintStream(System.out, LogLevel.INFO, 
    			(LogEventSink) console);
    	stderrloggingPrintStream = new LoggingPrintStream(System.err, LogLevel.ERROR, 
    			(LogEventSink) console);

    	System.setOut(stdoutLoggingPrintStream);
    	System.setErr(stderrloggingPrintStream);
		
    	return new Close() {
			
			@Override
			public void close() {
				stdoutLoggingPrintStream.flush();
				stderrloggingPrintStream.flush();
				
		    	System.setOut(originalStdOut);
		    	System.setErr(originalStdErr);
		    	
		    	console = null;
			}
		};
	}
	
	public static LogArchive console() {
		if (console == null) {
			throw new IllegalStateException("OddjobConsole not initialised.");
		}
		return console;
	}
	
	public interface Close extends AutoCloseable {
		
		@Override
		public void close();
	}
	
}
