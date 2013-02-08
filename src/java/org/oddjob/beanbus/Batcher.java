package org.oddjob.beanbus;

import java.util.Collection;
import java.util.List;

/**
 * Provide batching of beans. Unfinished and untested.
 * 
 * @author rob
 *
 * @param <T>
 */
public class Batcher<T> extends AbstractDestination<T>
implements BusAware {

	private int batchSize;

	private Collection<? super Iterable<T>> next;
	
	private List<T> batch;
	
	private int count;

	@Override
	public boolean add(T bean) {	

		batch.add(bean);
		if (++count == batchSize) {
			dispatch();
		}
		
		return true;
	}
	
	protected void dispatch() {
		if (count == 0) {
			return;
		}
		count = 0;

		next.add(batch);
		
		batch.clear();
	}

	@Override
	public void setBeanBus(BusConductor driver) {
		
		driver.addBusListener(new BusListenerAdapter() {
						
			@Override
			public void tripEnding(BusEvent event) throws BusCrashException {
				dispatch();				
			}
			
			@Override
			public void busTerminated(BusEvent event) {
				event.getSource().removeBusListener(this);
			}
			
		});
		
		if (next instanceof BusAware) {
			((BusAware) next).setBeanBus(driver);
		}
	}
	
	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	public Collection<? super Iterable<T>> getNext() {
		return next;
	}

	public void setNext(Collection<? super Iterable<T>> next) {
		this.next = next;
	}
}
