package org.oddjob.structural;

import org.oddjob.FailedToStopException;
import org.oddjob.Resetable;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Helper for managing child Objects. This class will track structural
 * changes and notify listeners.
 * 
 * @author Rob Gordon
 */

public class ChildHelper<E> 
implements Structural, Iterable<E>, ChildList<E> {
	private static final Logger logger = LoggerFactory.getLogger(ChildHelper.class);
	
	/** Contains the child jobs. */
	private final List<E> jobList =
			new ArrayList<>();
	
	/** Structural listeners. */
	private final List<StructuralListener> listeners =
			new ArrayList<>();
	
	/** Missed child actions. Allows a newly added listener to receive
	 * outstanding event while not missing a new event that arrives
	 * asynchronously. */
	private final Set<List<ChildAction>> missed =
			new HashSet<>();

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
	@Override
	public void insertChild(int index, E child) {
		if (child == null) {
			throw new NullPointerException("Attempt to add a null child.");
		}

		StructuralEvent event;
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
	 * Add a child to the end of the list.
	 * 
	 * @param child The child. Must not be null.
	 * 
	 * @return The index the child was added at.
	 */
	@Override
	public int addChild(E child) {
		if (child == null) {
			throw new NullPointerException("Attempt to add a null child.");
		}

		int index;
			
		StructuralEvent event;
		synchronized (missed) {
			index = jobList.size();
			jobList.add(index, child);
			event = new StructuralEvent(source, child, index);
			for (List<ChildAction> missing : missed) {
				missing.add(new ChildAdded(event));
			}
		}
		notifyChildAdded(event);
		
		return index;
	}
	
	/**
	 * Remove a child by index. This method
	 * fires the appropriate event in accordance with the Structural interface.
	 * 
	 * @param index The index of the child to remove.
	 * @return The child removed.
	 * 
	 * @throws IndexOutOfBoundsException If there is no child at the index.
	 */
	@Override
	public E removeChildAt(int index) throws IndexOutOfBoundsException {
		E child;
		
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
     * Insert or remove a child depending on if the child is null.
     *
     * @param index The index.
     * @param child The child.
     */
    public void insertOrRemoveChild(int index, E child) {
        if (child == null) {
            removeChildAt(index);
        }
        else {
            insertChild(index, child);
        }
    }

    /**
	 * Remove a child.
	 * 
	 * @param child The child to be removed.
	 * @return The index the child was removed from.
	 * 
	 * @throws IllegalStateException If the child is not our child.
	 */
	@Override
	public int removeChild(E child) throws IllegalStateException {
		int index;
		
		StructuralEvent event;
		synchronized (missed) {
			index = jobList.indexOf(child);
			
			if (index < 0) {
				throw new IllegalStateException("Failed removing child, [" + child + "] is not a child");
			}
			
			jobList.remove(child);
			
			event = new StructuralEvent(source, child, index);
			for (List<ChildAction> missing : missed) {
				missing.add(new ChildRemoved(event));
			}
		}
		
		notifyChildRemoved(event);
		return index;
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
			Object child = children[i];
			if (child instanceof Stoppable) {
				try {
					((Stoppable) child).stop();
				} 
				catch (FailedToStopException e) {
					failed = e;
					logger.debug("Child job [" + child + 
							"] has failed to stop." +
							" Continuing to stop any other children before throwing Exception.");
				} 
				catch (RuntimeException e) {
					failed = new FailedToStopException(child, 
							"[" + child + "] failed to stop.", e);
					logger.debug("Child job [" + child + 
							"] has thrown an Exception " + e.getMessage() + "." +
							" Continuing to stop any other children before throwing Exception.");
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
		for (Object child : children) {
			if (child instanceof Resetable) {
				((Resetable) child).softReset();
			}
		}
	}

	/**
	 * Perform a hard reset. This method propagates the hard reset message down
	 * to all child jobs. This is a convenience method a sub class can choose to use.
	 */
	public void hardResetChildren() {
		Object [] children = getChildren();
		for (Object child : children) {
			if (child instanceof Resetable) {
				((Resetable) child).hardReset();
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
			return jobList.toArray(new Object[0]);
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
	
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			int index;
			E next;
			
			@Override
			public boolean hasNext() {
				synchronized (missed) {
					// Work out the next index by adding one to the 
					// position of the last child in case a child has been removed.
					if (next != null) {
						int last = jobList.indexOf(next);
						if (last >= 0) {
							index = last + 1;
						}						
					}
					if (index < jobList.size()) {
						next = jobList.get(index);
					}
					else {
						next = null;
					}
				}
				return next != null;
			}
			@Override
			public E next() {
				synchronized (missed) {
					return next;
				}
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.structural.Structural#addStructuralListener(org.oddjob.structural.StructuralListener)
	 */
	public void addStructuralListener(StructuralListener listener) {
		List<ChildAction> ours = new ArrayList<>();
		synchronized (missed) {

			if  (listeners.contains(listener)) {
				throw new IllegalArgumentException("Listener " + listener + " already registered.");
			}

			for (int i = 0; i < jobList.size(); ++i) {
				StructuralEvent event = new StructuralEvent(source, jobList.get(i), i);
				ours.add(new ChildAdded(event));
			}
			missed.add(ours);
		}

		while (true) {
			ChildAction action;
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
	
	/**
	 * Returns true if there are no listeners listening for 
	 * {@link StructuralEvent}s.
	 * 
	 * @return true/false.
	 */
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
	
	/**
	 * Used to record child added/removed events.
	 */
	abstract static class ChildAction {
		
		protected final StructuralEvent event;
		
		ChildAction(StructuralEvent event) {
			this.event = event;
		}
		
		final void dispatch(StructuralListener listener) {
			try {
				doDispatch(listener);
			}
			catch (RuntimeException e) {
				logger.error("Failed dispatching " + 
						getClass().getSimpleName() + " event to listener " + 
						listener, e);
			}
		}
		
		
		abstract void doDispatch(StructuralListener listener) 
		throws RuntimeException;
	}
	
	/**
	 * Used to record a child added event.
	 */
	static class ChildAdded extends ChildAction {
		
		public ChildAdded(StructuralEvent event) {
			super(event);
		}
		
		@Override
		void doDispatch(StructuralListener listener) {
			
			listener.childAdded(event);			
		}
		
	}
	
	/**
	 * Used record a child removed event.
	 */
	static class ChildRemoved extends ChildAction {
		
		public ChildRemoved(StructuralEvent event) {
			super(event);
		}
		@Override
		public void doDispatch(StructuralListener listener) {
			
			listener.childRemoved(event);
		}
	}
	
	/**
	 * Notify the listeners.
	 * 
	 * @param event The event.
	 */
	private void notifyChildAdded(StructuralEvent event) {
		List<StructuralListener> copy;
		synchronized (missed) {
			copy = new ArrayList<>(listeners);
		}		
		for (StructuralListener l : copy) {
			new ChildAdded(event).dispatch(l);
		}
	}
	
	/*
	 * Notify the listeners.
	 * 
	 * @param event The event.
	 */
	private void notifyChildRemoved(StructuralEvent event) {
		List<StructuralListener> copy;
		synchronized (missed) {
			copy = new ArrayList<>(listeners);
		}		
		for (StructuralListener l : copy) {
			new ChildRemoved(event).dispatch(l);
		}
	}
		
	public static Object[] getChildren(Structural structural) {
		class ChildCatcher implements StructuralListener {
			final List<Object> results = new ArrayList<>();
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
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " for " + source;
	}
}
