/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import java.util.ArrayList;
import java.util.List;

import org.oddjob.OJConstants;
import org.oddjob.Oddjob;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.appender.AppenderArchiver;
import org.oddjob.logging.cache.LocalConsoleArchiver;
import org.oddjob.monitor.actions.ExplorerAction;
import org.oddjob.monitor.actions.ResourceActionProvider;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.util.ThreadManager;

/**
 * Data model for an explorer session.
 * 
 * @author Rob Gordon
 */
public class ExplorerModelImpl implements ExplorerModel {
//	private static final Logger logger = Logger.getLogger(ExplorerModel.class);
	
	/** The root node of the model. */
	private Oddjob oddjob; 
	
	/** Log Format */
	private String logFormat;
	
	/** Thread manager actions should use. */
	private ThreadManager threadManager;
	
	/** The logArchiver */
	private AppenderArchiver logArchiver;
	
	/** The console archiver. */
	private LocalConsoleArchiver consoleArchiver;
	

	private final ContextInitialiser[] contextInitialisers;
	
	private final ExplorerAction[] explorerActions;

	/**
	 * Constructor.
	 * 
	 * @param session The session used to load the actions.
	 */
	public ExplorerModelImpl(ArooaSession session) {
		explorerActions = new ResourceActionProvider(session
				).getExplorerActions();
		
		List<ContextInitialiser> initialisers = 
			new ArrayList<ContextInitialiser>();

		initialisers.add(new LogContextInialiser(this));
		initialisers.add(new ConfigContextInialiser(this));
		
		for (ExplorerAction action: explorerActions) {
			if (action instanceof ContextInitialiser) {
				initialisers.add((ContextInitialiser) action);
			}
		}
		
		contextInitialisers = initialisers.toArray(
				new ContextInitialiser[initialisers.size()]);
	}
	
	
	/**
	 * Set the model root node. This must be done before the model
	 * is used.
	 * 
	 * @param rootNode The root node.
	 */
	public void setOddjob(Oddjob rootNode) {
		this.oddjob = rootNode;
		logArchiver = new AppenderArchiver(rootNode, 
				logFormat == null ? OJConstants.DEFAULT_LOG_FORMAT : logFormat);
		consoleArchiver = new LocalConsoleArchiver();
	}

	/**
	 * Get the root node for this model.
	 * 
	 * @return The root node.
	 */
	public Oddjob getOddjob() {
		return oddjob;
	}
	
	/**
	 * Set the ThreadManager child actions should use.
	 * 
	 * @param threadManager The ThreadManager.
	 */
	public void setThreadManager(ThreadManager threadManager) {
		this.threadManager = threadManager;
	}
	
	/**
	 * Get an available ThreadManager.
	 * 
	 * @return A ThreadManager.
	 */
	public ThreadManager getThreadManager() {
		return threadManager;
	}
	
	/**
	 * Destroy this model.
	 *
	 */
	public void destroy() {
		logArchiver.onDestroy();
		consoleArchiver.onDestroy();
	}
	
	/**
	 * Getter for log format.
	 * 
	 * @return The log format.
	 */
	public String getLogFormat() {
		return logFormat;
	}
	
	/**
	 * The log format.
	 * 
	 * @param logFormat The log format.
	 */
	public void setLogFormat(String logFormat) {
		this.logFormat = logFormat;
	}
	
	/**
	 * Get the log archiver. This archiver is the top level archiver
	 * created for the root node.
	 * <p>
	 * For the archiver for a particular node use the {@link ExplorerContext}
	 * archiver.
	 * 
	 * @return A LogArchiver.
	 */
	public LogArchiver getLogArchiver() {
		return logArchiver;
	}
	
	/**
	 * Get the console archiver. This archiver is the top level archiver
	 * created for the root node.
	 * <p>
	 * For the archiver for a particular node use the {@link ExplorerContext}
	 * archiver.
	 * 
	 * @return A ConsoelArchiver.
	 */
	public ConsoleArchiver getConsoleArchiver() {
		return consoleArchiver;
	}

	public ContextInitialiser[] getContextInitialisers() {
		return contextInitialisers;
	}
	
	public ExplorerAction[] getExplorerActions() {
		return explorerActions;
	}
	
}
