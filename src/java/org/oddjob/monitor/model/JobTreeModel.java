package org.oddjob.monitor.model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
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

	/** Tree model listeners. */
	private final List<TreeModelListener> tmListeners = 
		Collections.synchronizedList(new ArrayList<TreeModelListener>());
	
	/** The root node. */
	private JobTreeNode root;
	
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
		tmListeners.add(tml);
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

	public void removeTreeModelListener(TreeModelListener tml) {
		tmListeners.remove(tml);
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		throw new UnsupportedOperationException("Don't need valueForPathChanged!");
	}

	public void fireTreeNodesChanged(TreeNode changed) {

		LinkedList<TreeNode> list = new LinkedList<TreeNode>();
		for (TreeNode i = changed; i != null; i = i.getParent()) {	
			list.addFirst(i);		
		}

		final TreeModelEvent event = new TreeModelEvent(changed, list.toArray());		

		Runnable r = new Runnable() {
			public void run() {		
				Iterable<TreeModelListener> copy = 
					new ArrayList<TreeModelListener>(tmListeners);
				
				for (final TreeModelListener tml : copy) {
					tml.treeNodesChanged(event);
				}
			}
		};
		
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			// Tree nodes changed is mainly for icon changes so we invoke later
			// Note that invoking now was causing Deadlock problems with designer.
			SwingUtilities.invokeLater(r);
		}
	}

	public void fireTreeNodesInserted(TreeNode changed, JobTreeNode child, int index) {

		LinkedList<TreeNode> list = new LinkedList<TreeNode>();
		
		for (TreeNode i = changed; i != null; i = i.getParent()) {
			list.addFirst(i);		
		}

		int childIndecies[] = { index };
		Object children [] = { child };

		final TreeModelEvent event = new TreeModelEvent(changed, list.toArray(), 
				childIndecies, children);

		Runnable r = new Runnable() {
			public void run() {
				Iterable<TreeModelListener> copy = 
					new ArrayList<TreeModelListener>(tmListeners);
				
				for (final TreeModelListener tml : copy) {
					tml.treeNodesInserted(event);
				}
			}
		};
				
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void fireTreeNodesRemoved(TreeNode changed, JobTreeNode child, int index) {

		LinkedList<TreeNode> list = new LinkedList<TreeNode>();
		
		for (TreeNode i = changed; i != null; i = i.getParent()) {	
			list.addFirst(i);		
		}

		int childIndecies[] = { index };
		Object children [] = { child };

		final TreeModelEvent event = new TreeModelEvent(changed, list.toArray(), 
				childIndecies, children);

		Runnable r = new Runnable() {
			public void run() {		
				Iterable<TreeModelListener> copy = 
					new ArrayList<TreeModelListener>(tmListeners);
				
				for (final TreeModelListener tml : copy) {
					tml.treeNodesRemoved(event);
				}
			}
		};
		
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
