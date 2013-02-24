package org.oddjob.beanbus.destinations;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.oddjob.arooa.life.Configured;
import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusListenerAdapter;

/**
 * A Queue for beans. A work in progress.
 * 
 * @author rob
 *
 * @param <E>
 */
public class BeanQueue<E> extends AbstractDestination<E>
implements Iterable<E> {

	private static final Logger logger = Logger.getLogger(BeanQueue.class);
	
	private int capacity;

	private volatile BlockingQueue<Object> queue;
	
	private final static Object STOP = new Object();
	
	private String name;
	
	private int taken;
	
	@Inject
	public void setBeanBus(BusConductor bus) {
		bus.addBusListener(new BusListenerAdapter() {
									
			@Override
			public void busStarting(BusEvent event) throws BusCrashException {
				logger.debug("Clearing Queue on Start.");
				queue.clear();
				taken = 0;
			}
			
			@Override
			public void busStopping(BusEvent event) throws BusCrashException {
				stop();
			}
		});
	}
	
	@Configured
	public void configured() {
		if (capacity == 0) {
			queue = new LinkedBlockingDeque<Object>();
		}
		else {
			queue = new ArrayBlockingQueue<Object>(capacity);
		}
	}
	
	public void stop() {
		logger.debug("Stopping Queue.");
		try {
			queue.put(STOP);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
	}
	
	@Override
	public boolean add(E bean) {
		try {
			queue.put(bean);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
		return true;
	}
	
	@Override
	public Iterator<E> iterator() {
		return new BlockerIterator();
	}
	
	/**
	 * The implementation of the blocking iterator.
	 */
	class BlockerIterator implements Iterator<E> {
		
		private E next;
		
		private int taken;
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean hasNext() {
			
			next = null;
			
			Object first = queue.poll();
			
			if (first == null) {

				// queue must be empty.
				try {
					first = queue.take();
				} catch (InterruptedException e) {
					return false;
				}				
			}
			
			if (first == STOP) {
				try {
					queue.put(STOP);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
				return false;
			}
			else {
				next = (E) first;
				
				++this.taken;
				++BeanQueue.this.taken;
				
				return true;
			}
		}
		
		@Override
		public E next() {
			return next;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String toString() {
			return "Iterator for " + BeanQueue.this.toString() + 
					", taken=" + taken;
		}
	}
	
	public int getSize() {
		Queue<?> queue = this.queue;
		return (queue == null ? 0: queue.size());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getTaken() {
		return taken;
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
