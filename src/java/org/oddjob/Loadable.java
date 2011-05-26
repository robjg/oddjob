package org.oddjob;


/**
 * An interface for components that can be loaded.
 *  
 * @author rob
 *
 */
public interface Loadable {

	/**
	 * Load the component.
	 */
	public void load();
	
	/**
	 * Unload the component.
	 */
	public void unload();
	
	/**
	 * Is the component currently loadable.
	 * 
	 * @return
	 */
	public boolean isLoadable();
	
	
}
