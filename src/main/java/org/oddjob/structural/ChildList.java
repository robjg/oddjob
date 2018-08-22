package org.oddjob.structural;

/**
 * A modifiable list of children.
 * 
 * @author rob
 *
 * @param <E> The type of the child.
 */
public interface ChildList<E> {

	/**
	 * Insert a child.
	 * 
	 * @param index The 0 based index of the insert.
	 * @param child The child. Must not be null.
	 */
	public void insertChild(int index, E child);
	
	/**
	 * Add a child to the end of the list.
	 * 
	 * @param child The child. Must not be null.
	 * 
	 * @return The index the child was added at.
	 */
	public int addChild(E child);
	
	/**
	 * Remove a child by index. 
	 * 
	 * @param index The index of the child to remove.
	 * @return The child removed.
	 * 
	 * @throws IndexOutOfBoundsException If there is no child at the index.
	 */
	public E removeChildAt(int index) throws IndexOutOfBoundsException;
	
	/**
	 * Remove a child.
	 * 
	 * @param child The child to be removed.
	 * @return The index the child was removed from.
	 * 
	 * @throws IllegalStateException If the child is not a child of this
	 * list.
	 */
	public int removeChild(Object child) throws IllegalStateException;
}
