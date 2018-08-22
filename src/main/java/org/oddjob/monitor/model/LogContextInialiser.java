package org.oddjob.monitor.model;

import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.context.ExplorerContext;

/**
 * A {@link ContextInitialiser} that adds logging information
 * to the context.
 * 
 * @author rob
 *
 */
public class LogContextInialiser implements ContextInitialiser {

	/** The key for the log archiver. */
	public static String LOG_ARCHIVER = "logArchiver";
	
	/** The key for the console archiver. */
	public static String CONSOLE_ARCHIVER = "consoleArchiver";
	
	/** The explorer model. */
	private final ExplorerModel explorerModel;
	
	/**
	 * Constructor.
	 * 
	 * @param explorerModel The model. Must not be null.
	 */
	public LogContextInialiser(ExplorerModel explorerModel) {
		this.explorerModel = explorerModel;
	}
	
	public void initialise(ExplorerContext context) {

		LogArchiver logArchiver;
		ConsoleArchiver consoleArchiver;
		
		ExplorerContext parent = context.getParent();
		
		if (parent == null) {
			
			// top level archivers.
			logArchiver = explorerModel.getLogArchiver();
			consoleArchiver = explorerModel.getConsoleArchiver();

		}
		else {

			// why change on parent logArchiver, not this logArchiver? Because
			// a LogArchiver will typically change for a client job. The client
			// job is a LogArchiver for it's remote nodes but it's log messages
			// go to it's parents log archiver - not it's own.
			if (parent.getThisComponent() instanceof LogArchiver) {
				logArchiver = ((LogArchiver) parent.getThisComponent()); 
			}
			else {
				logArchiver = (LogArchiver) parent.getValue(LOG_ARCHIVER);
			}
			
			// ditto for console archiver.
			if (parent.getThisComponent() instanceof ConsoleArchiver) {
				consoleArchiver = ((ConsoleArchiver) parent.getThisComponent()); 
			}
			else {
				consoleArchiver = (ConsoleArchiver) parent.getValue(CONSOLE_ARCHIVER);
			}
		}
		
		context.setValue(LOG_ARCHIVER, logArchiver);
		context.setValue(CONSOLE_ARCHIVER, consoleArchiver);
	}
}
