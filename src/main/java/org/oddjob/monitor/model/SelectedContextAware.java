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
	 * Called when the job selection changes. This is generally used
	 * to capture the new context and also clear any previous state for the
	 * last context.
	 * 
	 * @param context The context. Will never be null.
	 */
	public void setSelectedContext(ExplorerContext context);
	
	
	/**
	 * Called after the job has been selected, but before any action is
	 * to be performed. This is normally just before a menu is to be shown. 
	 */
	public void prepare();
	
}
