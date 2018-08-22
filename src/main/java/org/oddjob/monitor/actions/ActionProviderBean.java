package org.oddjob.monitor.actions;


/**
 * An {@link ActionProvider} that is a Java Bean.
 * 
 * @author rob
 *
 */
public class ActionProviderBean implements ActionProvider {

	/** Actions. */
	private ExplorerAction[] actions;
	
	/**
	 * Setter.
	 * 
	 * @param actions The Actions.
	 */
	public void setExplorerActions(ExplorerAction[] actions) {
		this.actions = actions;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.monitor.actions.ActionProvider#getExplorerActions()
	 */
	public ExplorerAction[] getExplorerActions() {
		return actions;
	}
}
