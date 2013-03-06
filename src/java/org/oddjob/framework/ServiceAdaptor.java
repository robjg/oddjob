package org.oddjob.framework;

/**
 * Something that adapts a component to be a {@link Service}.
 *  
 * @author rob
 *
 */
public interface ServiceAdaptor extends Service, ComponentAdapter {

	/**
	 * The component being adapted.
	 * 
	 * @return The component.
	 */
	@Override
	public Object getComponent();
}
