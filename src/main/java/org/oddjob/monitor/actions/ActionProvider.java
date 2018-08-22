package org.oddjob.monitor.actions;

/**
 * Provides {@link ExplorerAction}s.
 * 
 * @author rob
 *
 */
public interface ActionProvider {

	/**
	 * Return an array of {@link ExplorerAction}s.
	 * 
	 * @return ExplorerActions. Never null.
	 */
	public ExplorerAction[] getExplorerActions();
}
