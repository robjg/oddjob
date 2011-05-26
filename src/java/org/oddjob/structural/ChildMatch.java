package org.oddjob.structural;

import java.util.List;

abstract public class ChildMatch<T> {

	private final List<T> children;

	public ChildMatch(List<T> children) {
		this.children = children;
	}
	
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
	
	abstract protected void insertChild(int index, T child);
	
	abstract protected void removeChildAt(int index);
}
