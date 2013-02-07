package org.oddjob.beanbus;

import java.util.Iterator;

import org.oddjob.Stoppable;

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

	private Iterable<T> beans;
	
	private volatile boolean stop;
	
	private String name;
	
	@Override
	public void run() {
		
		if (beans == null) {
			throw new NullPointerException("No beans.");
		}
		
		stop = false;
		
		Iterator<T> current = beans.iterator();
		
		try {
			startBus();
		
			while (!stop && current.hasNext()) {
				
				accept(current.next());
			}			
			
			stopBus();
		} 
		catch (BusException e) {
				throw new RuntimeException(e);
		}
	}
	
	@Override
	public void stop() {
		this.stop = true;
	}
	
	@Override
	protected void requestStopBus() throws BusCrashException {
		stop();
	}
	
	public Iterable<T> getBeans() {
		return beans;
	}

	/**
	 * The beans to iterate over.
	 * 
	 * @param iterable
	 */
	public void setBeans(Iterable<T> iterable) {
		this.beans = iterable;
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
