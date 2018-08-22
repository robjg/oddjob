package org.oddjob.structural;

/**
 * A class which implements this interface is able to receive structural events.
 * 
 * @author Rob Gordon
 */

public interface StructuralListener {

	/**
	 * Called when a child is added to a Structural object.
	 * 
	 * @param event The strucural event.
	 */
	public void childAdded(StructuralEvent event);
	
	/**
	 * Called when a child is removed from a Strucutral object.
	 * 
	 * @param event The strucutral event.
	 */
	public void childRemoved(StructuralEvent event);
}
