package org.oddjob;

import org.oddjob.structural.StructuralListener;

/**
 * A class which implements this interface will inform listeners
 * when it's structure changes. A structural change is when a child
 * component is added or removed from the implementing class. A new
 * listener must receive add notifications for all existing children
 * as there is no other way to determine the existing structure of
 * an implementing class.
 * 
 * @author Rob Gordon
 */

public interface Structural {
	
	/**
	 * Add a listener. The listener will immediately receive add
	 * notifications for all existing children.
	 * 
	 * @param listener The listener.
	 */	
	public void addStructuralListener(StructuralListener listener);
	
	/**
	 * Remove a listener.
	 * 
	 * @param listener The listener.
	 */
	public void removeStructuralListener(StructuralListener listener);
}
