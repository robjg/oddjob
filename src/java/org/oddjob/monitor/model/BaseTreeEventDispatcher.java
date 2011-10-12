package org.oddjob.monitor.model;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;

/**
 * Common implementation for different {@link TreeEventDispatcher}s.
 * 
 * @author rob
 */
abstract public class BaseTreeEventDispatcher implements TreeEventDispatcher {

	private final List<TreeModelListener> listeners =
		new CopyOnWriteArrayList<TreeModelListener>();

	@Override
	public void addTreeModelListener(TreeModelListener tml) {
		listeners.add(tml);
	}
	
	@Override
	public void removeTreeModelListener(TreeModelListener tml) {
		listeners.remove(tml);
	}
	
	abstract protected void dispatch(Runnable runnable);
		
	private Object[] pathToRoot(TreeNode changed) {
		LinkedList<TreeNode> list = new LinkedList<TreeNode>();
		for (TreeNode i = changed; i != null; i = i.getParent()) {	
			list.addFirst(i);		
		}		
		return list.toArray(new Object[list.size()]);
	}
	
	public synchronized void fireTreeNodesChanged(TreeNode changed) {

		final TreeModelEvent event = new TreeModelEvent(
				changed, pathToRoot(changed));		

		Runnable r = new Runnable() {
			public void run() {		
				for (final TreeModelListener tml : listeners) {
					tml.treeNodesChanged(event);
				}
			}
		};
		
		dispatch(r);
	}

	public synchronized void fireTreeNodesInserted(TreeNode changed, JobTreeNode child, int index) {

		int childIndecies[] = { index };
		Object children [] = { child };

		final TreeModelEvent event = new TreeModelEvent(changed, 
				pathToRoot(changed), 
				childIndecies, children);

		Runnable r = new Runnable() {
			public void run() {
				for (final TreeModelListener tml : listeners) {
					tml.treeNodesInserted(event);
				}
			}
		};
				
		dispatch(r);
	}

	public synchronized void fireTreeNodesRemoved(TreeNode changed, JobTreeNode child, int index) {

		int childIndecies[] = { index };
		Object children [] = { child };

		final TreeModelEvent event = new TreeModelEvent(changed, 
				pathToRoot(changed), 
				childIndecies, children);

		Runnable r = new Runnable() {
			public void run() {		
				for (final TreeModelListener tml : listeners) {
					tml.treeNodesRemoved(event);
				}
			}
		};
		
		dispatch(r);
	}
}
