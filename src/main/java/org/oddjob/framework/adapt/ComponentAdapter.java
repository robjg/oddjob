package org.oddjob.framework.adapt;

/**
 * Something that adapts a component to implement an interface.
 * 
 * @author rob
 *
 */
public interface ComponentAdapter {

	/**
	 * Get the component being adapted.
	 * 
	 * @return The component. Never null.
	 */
	Object getComponent();
}
