package org.oddjob.beanbus.destinations;

import org.oddjob.Stoppable;
import org.oddjob.arooa.life.Configured;
import org.oddjob.arooa.life.Initialised;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.TrackingBusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

/**
 * @oddjob.description A Queue for beans. A work in progress.
 * 
 * @oddjob.example 
 * 
 * A simple example.
 * 
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanQueueExample.xml}
 * 
 * @oddjob.example 
 * 
 * An example in BeanBus.
 * 
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BeanQueueExample2.xml}
 * 
 * 
 * @author rob
 *
 * @param <E> The type of element on the queue.
 */
public class BeanQueue<E> implements Consumer<E>, Iterable<E>, Stoppable {

	private static final Logger logger = LoggerFactory.getLogger(BeanQueue.class);
	
	private final static Object STOP = new Object();
	
	private volatile int capacity;

	private volatile BlockingQueue<Object> queue;
	
	private volatile String name;
	
	private volatile int taken;
	
	private volatile int waitingConusmers;
	
	private final TrackingBusListener busListener = 
			new TrackingBusListener() {
		@Override
		public void busStarting(BusEvent event) {
			logger.debug("Clearing Queue on Start.");
			reset();
		}
		
		@Override
		public void busStopping(BusEvent event) {
			stop();
		}		
	};
	
	@Inject
	public void setBeanBus(BusConductor busConductor) {
		busListener.setBusConductor(busConductor);
	}
	
	@Initialised
	public void init() {
		if (capacity == 0) {
			queue = new LinkedBlockingDeque<>();
		}
		else {
			queue = new ArrayBlockingQueue<>(capacity);
		}
	}
	
	@Configured
	public void reset() {
		queue.clear();
		taken = 0;
	}
	
	public void stop() {
		logger.debug("Stopping Queue.");
		try {
			queue.put(STOP);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void accept(E  bean) {
		try {
			queue.put(bean);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
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
			
			if (next != null) {
				return true;
			}
			
			Object first = queue.poll();
			
			if (first == null) {

				// queue must be empty.
				try {
					++waitingConusmers;
					
					first = queue.take();
				} catch (InterruptedException e) {
					logger.info("Inturrupted waiting for next value.");
					Thread.currentThread().interrupt();
					return false;
				}				
				finally {
					--waitingConusmers;
				}
			}
			
			if (first == STOP) {
				try {
					queue.put(STOP);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
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
			try {
				return next;
			}
			finally {
				next = null;
			}
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

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		if (this.queue != null) {
			throw new IllegalStateException(
					"Capicity can't be dynamic because the queue has already been created.");
		}
		this.capacity = capacity;
	}

	public int getWaitingConusmers() {
		return waitingConusmers;
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
