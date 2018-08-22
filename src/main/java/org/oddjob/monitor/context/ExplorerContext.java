/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.context;

import org.oddjob.util.ThreadManager;

/**
 * Explorer Context. Used to pass useful things down the job hierarchy.
 * <p>
 * A unique context will exist for each node in the hierarchy but where
 * as the model has specific information about the node in the tree - it's
 * children, is it visible etc, the context contains ancillary information
 * about the nodes environment. 
 * 
 * @author Rob Gordon
 */
public interface ExplorerContext {
	
	/**
	 * Get the component this is the context for.
	 * 
	 * @return
	 */
	public Object getThisComponent();
	
	/**
	 * Add a child context.
	 *
	 * @param child The component the child is for.
	 * @return A child context for the component.
	 */
	public ExplorerContext addChild(Object child);
	
	
	public ThreadManager getThreadManager();

	/**
	 * Get the parent context of this context.
	 * 
	 * @return The parent, or null if this is the root.
	 */
	public ExplorerContext getParent();
	
	/**
	 * Set a value in the context.
	 * 
	 * @param key An identifier for the value.
	 * @param value The value.
	 */
	public void setValue(String key, Object value);
	
	/**
	 * Get a value from the context.
	 * 
	 * @param key The identifier.
	 * 
	 * @return The value, or null.
	 */
	public Object getValue(String key);
}
