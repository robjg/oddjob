package org.oddjob.monitor.actions;

import org.oddjob.arooa.design.screem.Form;

/**
 * An {@link ExplorerAction} that requires a form
 * for further input.
 * 
 * @author rob
 *
 */
public interface FormAction extends ExplorerAction {
	
	/**
	 * Provide the form. 
	 * 
	 * @return The DesignDefinition for the form. Never Null.
	 */
	public Form form();

}
