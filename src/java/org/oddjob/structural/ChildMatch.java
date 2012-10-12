package org.oddjob.structural;

import java.util.List;

/**
 * Compares an incoming list of children with a base list and performs
 * insert and remove operations that make the lists match.
 * <p>
 * This is an abstract class. Sub classes provide the insert and remove
 * operations.
 *  
 * @author rob
 *
 * @param <T> The type of the children.
 */
abstract public class ChildMatch<T> {

	private final List<T> children;

	/**
	 * Create an instance with a list of children.
	 * 
	 * @param children
	 */
	public ChildMatch(List<T> children) {
		this.children = children;
	}
	
	/**
	 * Match the array to our list.
	 * 
	 * @param match
	 */
	public void match(T[] match) {
				
		for (int i = 0; i < match.length; ++i) {
			
			T other = match[i];
			
			if (children.size() <= i) {
				trackInsertChild(i, other);
				continue;
			}
			Object ours = children.get(i);
			
			if (ours.equals(other)) {
				continue;
			}
			
			if (children.contains(other)) {
				trackRemoveChildAt(i);
				--i;
			}
			else {
				trackInsertChild(i, other);
			}	
		}
		
		while (children.size() > match.length) {
			trackRemoveChildAt(match.length);
		}
	}
	
	private void trackInsertChild(int index, T child) {
		children.add(index, child);
		insertChild(index, child);
	}
	
	private void trackRemoveChildAt(int index) {
		children.remove(index);
		removeChildAt(index);
	}
	
	/**
	 * Sub classes provide implementation.
	 * 
	 * @param index
	 * @param child
	 */
	abstract protected void insertChild(int index, T child);
	
	/**
	 * Sub classes provide implementation.
	 * @param index
	 */
	abstract protected void removeChildAt(int index);
}
