package org.oddjob.beanbus;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.oddjob.arooa.life.Configured;

/**
 * A Queue for beans. A work in progress.
 * 
 * @author rob
 *
 * @param <E>
 */
public class BeanQueue<E> implements Destination<E>, Iterable<E>, BusAware {

	private int capacity;

	private BlockingQueue<E> queue;
	
	
	private Set<Thread> threads = Collections.synchronizedSet(
			new HashSet<Thread>());
	
	@Override
	public void setBeanBus(BusConductor bus) {
		bus.addBusListener(new BusListenerAdapter() {
									
			@Override
			public void busTerminated(BusEvent event) {
				stop();
				event.getSource().removeBusListener(this);
			}
		});
	}
	
	@Configured
	private void configured() {
		if (capacity == 0) {
			queue = new LinkedBlockingDeque<E>();
		}
		else {
			queue = new ArrayBlockingQueue<E>(capacity);
		}
	}
	
	public void stop() {
		synchronized (threads) {
			for (Thread thread: threads) {
				thread.interrupt();
			}
		}
	}
	
	@Override
	public void accept(E bean) throws BadBeanException, BusCrashException {
		try {
			queue.put(bean);
		} catch (InterruptedException e) {
			Thread.interrupted();
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
		
		@Override
		public boolean hasNext() {
			
			next = queue.poll();
			if (next != null) {
				return true;
			}
			
			threads.add(Thread.currentThread());
			try {
				next = queue.take();
			} catch (InterruptedException e) {
				return false;
			}
			finally {
				threads.remove(Thread.currentThread());
			}
			return true;
		}
		
		@Override
		public E next() {
			return next;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
}
