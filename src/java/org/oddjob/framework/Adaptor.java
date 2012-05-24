package org.oddjob.framework;

/**
 * Something that adapts a component to implement an interface.
 * 
 * @author rob
 *
 */
public interface Adaptor {

	/**
	 * Get the component being adapted.
	 * 
	 * @return The component. Never null.
	 */
	public Object getComponent();
}
