package org.oddjob.beanbus.drivers;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.oddjob.Stoppable;
import org.oddjob.beanbus.AbstractBusComponent;
import org.oddjob.beanbus.BeanBus;
import org.oddjob.beanbus.BusException;
import org.oddjob.framework.HardReset;
import org.oddjob.framework.SoftReset;

/**
 * A Runnable that can be used as an Oddjob job to take beans from an
 * iterable (collection) and drive them into an {@link BeanBus}.
 * 
 * @author rob
 *
 * @param <T>
 */
public class IterableBusDriver<T> extends AbstractBusComponent<T> 
implements Runnable, Stoppable {

	private static final Logger logger = Logger.getLogger(IterableBusDriver.class);
	
	private Iterable<? extends T> beans;
	
	private volatile boolean stop;
	
	private String name;
	
	private volatile int count;
	
	private volatile Thread executionThread;
	
	@HardReset
	@SoftReset
	public void reset() {
		count = 0;
	}
	
	@Override
	public void run() {
		
		if (beans == null) {
			throw new NullPointerException("No beans.");
		}
		
		stop = false;
		
		Iterator<? extends T> current = beans.iterator();
		
		try {
			startBus();
		
			while (!stop) {
				synchronized (this) {
					executionThread = Thread.currentThread(); 
				}
				if (!current.hasNext()) {
					break;
				}
				synchronized (this) {
					executionThread = null;
					Thread.interrupted();
				}
				
				accept(current.next());
				
				++count;
			}			
			
			stopBus();
			
			logger.info("Accepted " + count + " beans.");
		} 
		catch (BusException e) {
				throw new RuntimeException(e);
		}
	}
	
	@Override
	public void stop() {
		requestBusStop();
	}
	
	@Override
	protected void stopTheBus() {
		this.stop = true;
		synchronized (this) {
			if (executionThread != null) {
				logger.debug("Interrupting execution thread.");
				executionThread.interrupt();
			}
		}
	}
	
	public Iterable<? extends T> getBeans() {
		return beans;
	}

	/**
	 * The beans to iterate over.
	 * 
	 * @param iterable
	 */
	public void setBeans(Iterable<? extends T> iterable) {
		this.beans = iterable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getCount() {
		return count;
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
