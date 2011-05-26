package org.oddjob.structural;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oddjob.FailedToStopException;
import org.oddjob.Resetable;
import org.oddjob.Stoppable;
import org.oddjob.Structural;

/**
 * Helper for managing child Objects. This class will track structural
 * changes and notify listeners.
 * 
 * @author Rob Gordon
 */

public class ChildHelper<E> implements Structural {

	/** Contains the child jobs. */
	private final List<E> jobList = 
		new ArrayList<E>();
	
	/** Structural listeners. */
	private final List<StructuralListener> listeners = 
		new ArrayList<StructuralListener>();
	
	/** Missed child actions. Allows a newly added listener to receive
	 * outstanding event while not missing a new event that arrives
	 * asynchronously. */
	private final Set<List<ChildAction>> missed = 
		new HashSet<List<ChildAction>>();

	/** The source. */
	private final Structural source;
	
	/**
	 * Constructor.
	 * 
	 * @param source The source used as the source of the event.
	 */
	public ChildHelper(Structural source) {
		this.source = source;
	}

	/**
	 * Insert a child.
	 * 
	 * @param index The index.
	 * @param child The child.
	 */
	public void insertChild(int index, E child) {
		if (child == null) {
			throw new NullPointerException("Attempt to add a null child.");
		}

		StructuralEvent event = null;
		synchronized (missed) {
			jobList.add(index, child);
			event = new StructuralEvent(source, child, index);
			for (List<ChildAction> missing : missed) {
				missing.add(new ChildAdded(event));
			}
		}
		notifyChildAdded(event);
	}

	
	/**
	 * Remove a child by index. This method
	 * fires the appropriate event in accordance with the Strucutral interface.
	 * 
	 * @param index The index of the child to remove.
	 * @return The child removed.
	 */
	public Object removeChildAt(int index) {
		Object child = null;
		
		StructuralEvent event;
		synchronized (missed) {
			child = jobList.remove(index);
			event = new StructuralEvent(source, child, index);
			for (List<ChildAction> missing : missed) {
				missing.add(new ChildRemoved(event));
			}
		}
		
		notifyChildRemoved(event);
		return child;
	}

	/**
	 * Allows a sub class to remove all children from itself. This method 
	 * fires the appropriate events in accordance with the structural interface.
	 * <p>
	 * This method isn't synchronized. Simultaneous
	 * removal of children by a different thread could result in an 
	 * IndexOutOfBoundsException.
	 * <p>
	 */
	public void removeAllChildren() {
		while (true) {
			int size = jobList.size();
			if (size == 0) {
				break;
			}
			removeChildAt(size - 1);
		}
	}

	/**
	 * Stops all the child jobs. Jobs are stopped in reverse order. 
	 */
	public void stopChildren() throws FailedToStopException {
		Object [] children = getChildren();
		FailedToStopException failed = null;
		for (int i = children.length - 1; i > -1; --i) {
			if (children[i] instanceof Stoppable) {
				try {
					((Stoppable)children[i]).stop();
				} catch (FailedToStopException e) {
					failed = e;
				}
			}
		} 
		if (failed != null) {
			throw failed;
		}
	}

	/**
	 * Perform a soft reset. This method propagates the soft reset message down to
	 * all child jobs. This is a convenience method that a sub class can choose to use.
	 */
	public void softResetChildren() {
		Object [] children = getChildren();
		for (int i = 0; i < children.length; ++i) {			
			if (children[i] instanceof Resetable) {
				((Resetable)children[i]).softReset();
			}
		}
	}

	/**
	 * Perform a hard reset. This method propergates the hard reset message down
	 * to all child jobs. This is a convenience method a sub class can choose to use.
	 */
	public void hardResetChildren() {
		Object [] children = getChildren();
		for (int i = 0; i < children.length; ++i) {			
			if (children[i] instanceof Resetable) { 
				((Resetable)children[i]).hardReset();
			}
		}
	}

	/**
	 * Return an array of children.
	 * 
	 * @return An array of child objects.
	 */
	public Object[] getChildren() {
		synchronized (missed) {
			return jobList.toArray(new Object[jobList.size()]);
		}
	}	
	
	/**
	 * Return an array of children.
	 * 
	 * @return An array of child objects.
	 */
	public E[] getChildren(E[] array) {
		synchronized (missed) {
			return jobList.toArray(array);
		}
	}	
	
	/**
	 * Return a child.
	 * 
	 * @return A child.
	 */
	public E getChildAt(int index) {
		synchronized (missed) {
			return jobList.get(index);
		}
	}	
	
	/**
	 * Return an only child.
	 * 
	 * @return A child.
	 */
	public E getChild() {
		synchronized (missed) {
			if (jobList.size() == 0) {
				return null;
			}
			if (jobList.size() > 1) {
				throw new IllegalStateException("Can't use getChild with more than one child!");
			}
			return jobList.get(0);
		}
	}	

	/**
	 * Is this child ours?
	 * 
	 * @param child
	 * 
	 * @return true if it is, false if it isn't.
	 */
	public boolean contains(E child) {
		synchronized (missed) {
			return jobList.contains(child);
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.structural.Structural#addStructuralListener(org.oddjob.structural.StructuralListener)
	 */
	public void addStructuralListener(StructuralListener listener) {
		List<ChildAction> ours = new ArrayList<ChildAction>();
		synchronized (missed) {
			for (int i = 0; i < jobList.size(); ++i) {
				StructuralEvent event = new StructuralEvent(source, jobList.get(i), i);
				ours.add(new ChildAdded(event));
			}
			missed.add(ours);
		}

		while (true) {
			ChildAction action = null;
			synchronized (missed) {
				if (ours.isEmpty()) {
					missed.remove(ours);
					listeners.add(listener);
					break;
				}
				else {
					action = ours.remove(0);
				}
			}
			if (action != null) {
				action.dispatch(listener);
			}
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.structural.Structural#removeStructuralListener(org.oddjob.structural.StructuralListener)
	 */
	public void removeStructuralListener(StructuralListener listener) {
		synchronized (missed) {
			listeners.remove(listener);
		}
	}	
	
	public boolean isNoListeners() {
		synchronized (missed) {
			return listeners.isEmpty();
		}
	}
	
	/**
	 * The number of children.
	 * 
	 * @return The number of children.
	 */
	public int size() {
		synchronized (missed) {
			return jobList.size();
		}
	}
	
	abstract class ChildAction {
		
		protected final StructuralEvent event;
		
		ChildAction(StructuralEvent event) {
			this.event = event;
		}
		
		abstract public void dispatch(StructuralListener listener);
	}
	
	class ChildAdded extends ChildAction {
		
		public ChildAdded(StructuralEvent event) {
			super(event);
		}
		@Override
		public void dispatch(StructuralListener listener) {
			listener.childAdded(event);
			
		}
	}
	
	class ChildRemoved extends ChildAction {
		
		public ChildRemoved(StructuralEvent event) {
			super(event);
		}
		@Override
		public void dispatch(StructuralListener listener) {
			listener.childRemoved(event);
			
		}
	}
	
	/**
	 * Notify the listeners.
	 * 
	 * @param event The event.
	 */
	private void notifyChildAdded(StructuralEvent event) {
		List<StructuralListener> copy = null;
		synchronized (missed) {
			copy = new ArrayList<StructuralListener>(listeners);
		}		
		for (StructuralListener l : copy) {
			l.childAdded(event);
		}
	}
	
	/*
	 * Notify the listeners.
	 * 
	 * @param event The event.
	 */
	private void notifyChildRemoved(StructuralEvent event) {
		List<StructuralListener> copy = null;
		synchronized (missed) {
			copy = new ArrayList<StructuralListener>(listeners);
		}		
		for (StructuralListener l : copy) {
			l.childRemoved(event);
		}
	}
		
	public static Object[] getChildren(Structural structural) {
		class ChildCatcher implements StructuralListener {
			List<Object> results = new ArrayList<Object>();
			public void childAdded(StructuralEvent event) {
				synchronized (results) {
					results.add(event.getIndex(), event.getChild());
				}
			}
			public void childRemoved(StructuralEvent event) {
				synchronized (results) {
					results.remove(event.getIndex());
				}
			}
		}
		ChildCatcher cc = new ChildCatcher();
		structural.addStructuralListener(cc);
		structural.removeStructuralListener(cc);		
		return cc.results.toArray();
	}
}
