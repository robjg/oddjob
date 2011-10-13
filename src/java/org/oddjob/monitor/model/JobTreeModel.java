package org.oddjob.monitor.model;

import java.util.concurrent.Executor;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


/**
 * A model which provides the swing tree model of the Oddjob structure.
 * 
 * @author Rob Gordon 
 */
public class JobTreeModel implements TreeModel {

	private final TreeEventDispatcher eventDispatcher;
	
	/** The root node. */
	private JobTreeNode root;
	
	public JobTreeModel() {
		this(new ExecutorTreeEventDispatcher(new EventThreadOnlyDispatcher()));
	}
		
	public JobTreeModel(Executor executor) {
		this(new ExecutorTreeEventDispatcher(executor));
	}
	
	public JobTreeModel(TreeEventDispatcher eventDispatcher) {
		if (eventDispatcher == null) {
			throw new NullPointerException("No EventDispatcher.");
		}
		
		this.eventDispatcher = eventDispatcher;
	}
	
	/**
	 * Set the root tree node.
	 * 
	 * @param node The top of the tree.
	 */	
	public void setRootTreeNode(JobTreeNode node) {
		this.root = node;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void addTreeModelListener(TreeModelListener tml) {
		eventDispatcher.addTreeModelListener(tml);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void removeTreeModelListener(TreeModelListener tml) {
		eventDispatcher.removeTreeModelListener(tml);
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	public Object getChild(Object parent, int index) {
		return ((JobTreeNode)parent).getChildAt(index);
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	public boolean isLeaf(Object node) {
		return ((JobTreeNode)node).isLeaf();		
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	public int getChildCount(Object parent) {
		return ((JobTreeNode)parent).getChildCount();
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	public int getIndexOfChild(Object parent, Object child) {
		return ((JobTreeNode)parent).getIndex((JobTreeNode)child);	
	}
	
	/*
	 *  (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	public Object getRoot() {
		return root;
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		throw new UnsupportedOperationException("Don't need valueForPathChanged!");
	}

	public void fireTreeNodesChanged(TreeNode changed) {

		eventDispatcher.fireTreeNodesChanged(changed);		
	}

	public void fireTreeNodesInserted(TreeNode changed, JobTreeNode child, int index) {

		eventDispatcher.fireTreeNodesInserted(changed, child, index);
	}

	public void fireTreeNodesRemoved(TreeNode changed, JobTreeNode child, int index) {

		eventDispatcher.fireTreeNodesRemoved(changed, child, index);
	}
}
