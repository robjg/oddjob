package org.oddjob.monitor.model;

import org.oddjob.monitor.context.ExplorerContext;

/**
 * Something that interacts with the selected {@link ExplorerContext}.
 * 
 * @author rob
 *
 */
public interface SelectedContextAware {

	/**
	 * 
	 * @param context
	 */
	public void setSelectedContext(ExplorerContext context);
	
	
	/**
	 * 
	 */
	public void prepare();
	
}
