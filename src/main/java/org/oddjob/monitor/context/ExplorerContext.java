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
public interface ExplorerContext extends AncestorContext {
	
	/**
	 * Add a child context.
	 *
	 * @param child The component the child is for.
	 * @return A child context for the component.
	 */
	ExplorerContext addChild(Object child);
	
	
	ThreadManager getThreadManager();

	/**
	 * Get the parent context of this context.
	 * 
	 * @return The parent, or null if this is the root.
	 */
	@Override
	ExplorerContext getParent();
	
	/**
	 * Set a value in the context.
	 * 
	 * @param key An identifier for the value.
	 * @param value The value.
	 */
	void setValue(String key, Object value);
	
	/**
	 * Get a value from the context.
	 * 
	 * @param key The identifier.
	 * 
	 * @return The value, or null.
	 */
	Object getValue(String key);
}
