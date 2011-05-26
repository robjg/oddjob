package org.oddjob.monitor.model;

import org.oddjob.Oddjob;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.monitor.actions.ExplorerAction;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.util.ThreadManager;


public class MockExplorerModel implements ExplorerModel {

	public void destroy() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public ConsoleArchiver getConsoleArchiver() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public LogArchiver getLogArchiver() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public String getLogFormat() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public Oddjob getOddjob() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public ThreadManager getThreadManager() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public ContextInitialiser[] getContextInitialisers() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public ExplorerAction[] getExplorerActions() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
