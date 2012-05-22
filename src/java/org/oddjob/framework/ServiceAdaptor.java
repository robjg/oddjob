package org.oddjob.framework;

/**
 * Something that adapts a component to be a {@link Service}.
 *  
 * @author rob
 *
 */
public interface ServiceAdaptor extends Service {

	/**
	 * The component being adapted.
	 * 
	 * @return The component.
	 */
	public Object getComponent();
}
