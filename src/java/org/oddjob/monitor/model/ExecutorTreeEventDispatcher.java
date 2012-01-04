package org.oddjob.monitor.model;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;

/**
 * An implementation of a {@link TreeEventDispatcher} that uses
 * an Executor.
 * 
 * @author rob
 */
public class ExecutorTreeEventDispatcher implements TreeEventDispatcher {

	/** The listeners. */
	private final List<TreeModelListener> listeners =
		new CopyOnWriteArrayList<TreeModelListener>();

	/** The executor used to fire events. */
	private final Executor executor;
	
	/**
	 * Only Constructor.
	 * 
	 * @param executor The executor.
	 */
	public ExecutorTreeEventDispatcher(Executor executor) {
		this.executor = executor;
	}

	@Override
	public synchronized void addTreeModelListener(TreeModelListener tml) {
		listeners.add(tml);
	}
	
	@Override
	public synchronized void removeTreeModelListener(TreeModelListener tml) {
		listeners.remove(tml);
	}
	
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

		Runnable runnable = new Runnable() {
			public void run() {		
				for (final TreeModelListener tml : listeners) {
					tml.treeNodesChanged(event);
				}
			}
		};
		
		executor.execute(runnable);
	}

	public synchronized void fireTreeNodesInserted(TreeNode changed, JobTreeNode child, int index) {

		int childIndecies[] = { index };
		Object children [] = { child };

		final TreeModelEvent event = new TreeModelEvent(changed, 
				pathToRoot(changed), 
				childIndecies, children);

		Runnable runnable = new Runnable() {
			public void run() {
				for (final TreeModelListener tml : listeners) {
					tml.treeNodesInserted(event);
				}
			}
		};
				
		executor.execute(runnable);
	}

	public synchronized void fireTreeNodesRemoved(TreeNode changed, JobTreeNode child, int index) {

		int childIndecies[] = { index };
		Object children [] = { child };

		final TreeModelEvent event = new TreeModelEvent(changed, 
				pathToRoot(changed), 
				childIndecies, children);

		Runnable runnable = new Runnable() {
			public void run() {		
				for (final TreeModelListener tml : listeners) {
					tml.treeNodesRemoved(event);
				}
			}
		};
		
		executor.execute(runnable);
	}
}
