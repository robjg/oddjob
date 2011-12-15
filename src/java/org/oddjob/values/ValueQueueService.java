package org.oddjob.values;

import java.util.Iterator;
import java.util.LinkedList;

import org.oddjob.jobs.structural.ForEachJob;

/**
 * Provide a service that supports a blocking Iterable that can be
 * used as the values for a {@link ForEachJob}.
 * <p>
 * Values are placed on the queue by setting the value property.
 * <p>
 * When this service is stopped any iterators in use return false from
 * their hasNext method. This will cause the foreach job to complete.
 * 
 * @author rob
 *
 */
public class ValueQueueService {

	/** The queue. */
	private final LinkedList<Object> queue = new LinkedList<Object>();

	/** Has the service been started. */
	private boolean started;

	/** The name of this service. */
	private String name;
	
	/**
	 * Start.
	 */
	public void start() {
		synchronized (queue) {
			started = true;
		}
	}
	
	/**
	 * Stop.
	 */
	public void stop() {
		started = false;
		synchronized(queue) {
			queue.notifyAll();
		}
	}
	
	/**
	 * The implementation of the blocking iterator.
	 */
	class BlockerIterator implements Iterator<Object> {
		
		private Object next;
		
		@Override
		public boolean hasNext() {
			while (started) {
				synchronized (queue) {
					if (queue.isEmpty()) {
						try {
							queue.wait();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							return false;
						}
					} else {
						next = queue.removeFirst();
						return true;
					}
				}
			}
			return false;
		}
		
		@Override
		public Object next() {
			return next;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
		
	/**
	 * Get the values.
	 * 
	 * @return A blocking Iterable. Never null.
	 */
	public Iterable<Object> getValues() {
		
		return new Iterable<Object>() {
			
			@Override
			public Iterator<Object> iterator() {
				return new BlockerIterator();
			}
		};
	}
	
	/**
	 * Set a value. This causes the value to be placed on the queue.
	 * 
	 * @param object
	 */
	public void setValue(Object object) {
		synchronized (queue) {
			if (!started) {
				throw new IllegalStateException(this + " is not started.");
			}
			queue.add(object);
			queue.notifyAll();
		}
	}

	/**
	 * Get the current size of the queue.
	 * 
	 * @return The size.
	 */
	public int getSize() {
		synchronized (queue) {
			return queue.size();
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}
}
