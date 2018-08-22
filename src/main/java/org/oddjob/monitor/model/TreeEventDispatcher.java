package org.oddjob.monitor.model;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;

/**
 * Abstraction for something able to manage {@link TreeModelListener}s
 * and dispatch events to them.
 * 
 * @author rob
 *
 */
public interface TreeEventDispatcher {
	
	/**
	 * Add a {@link TreeModelListener}.
	 * 
	 * @param tml The listener.
	 */
	public void addTreeModelListener(TreeModelListener tml);

	/**
	 * Remove a {@link TreeModelListener}.
	 * 
	 * @param tml The lienter.
	 */
	public void removeTreeModelListener(TreeModelListener tml);

	/**
	 * Notify all listeners of a tree node changed event.
	 * 
	 * @param changed The node that's changed.
	 */
	public void fireTreeNodesChanged(TreeNode changed);

	/**
	 * Fire a tree node inserted event.
	 * 
	 * @param changed The parent node.
	 * @param child The child.
	 * @param index The index the child has been inserted at.
	 */
	public void fireTreeNodesInserted(TreeNode changed, JobTreeNode child, int index);

	/**
	 * Fire a tree node removed event.
	 * 
	 * @param changed The parent node.
	 * @param child The node removed.
	 * @param index The index of where the node was removed from.
	 */
	public void fireTreeNodesRemoved(TreeNode changed, JobTreeNode child, int index);
}
