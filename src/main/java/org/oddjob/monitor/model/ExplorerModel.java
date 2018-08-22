/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import org.oddjob.Oddjob;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.monitor.actions.ExplorerAction;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.util.ThreadManager;

/**
 * Data model for an explorer session.
 * 
 * @author Rob Gordon
 */
public interface ExplorerModel {

	/**
	 * Get the root node for this model.
	 * 
	 * @return The root node.
	 */
	public Oddjob getOddjob();
	
	/**
	 * Get an available ThreadManager.
	 * 
	 * @return A ThreadManager.
	 */
	public ThreadManager getThreadManager();
	
	
	/**
	 * Getter for log format.
	 * 
	 * @return The log format.
	 */
	public String getLogFormat();
	
	
	/**
	 * Get the log archiver. This archiver is the top level archiver
	 * created for the root node.
	 * <p>
	 * For the archiver for a particular node use the {@link ExplorerContext}
	 * archiver.
	 * 
	 * @return A LogArchiver.
	 */
	public LogArchiver getLogArchiver();
	
	/**
	 * Get the console archiver. This archiver is the top level archiver
	 * created for the root node.
	 * <p>
	 * For the archiver for a particular node use the {@link ExplorerContext}
	 * archiver.
	 * 
	 * @return A ConsoelArchiver.
	 */
	public ConsoleArchiver getConsoleArchiver();
	
	
	public ContextInitialiser[] getContextInitialisers();
	
	public ExplorerAction[] getExplorerActions();
	
	/**
	 * Destroy this model.
	 *
	 */
	public void destroy();
}
