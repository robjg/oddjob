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
	
	public void addTreeModelListener(TreeModelListener tml);

	public void removeTreeModelListener(TreeModelListener tml);

	public void fireTreeNodesChanged(TreeNode changed);

	public void fireTreeNodesInserted(TreeNode changed, JobTreeNode child, int index);

	public void fireTreeNodesRemoved(TreeNode changed, JobTreeNode child, int index);
}
