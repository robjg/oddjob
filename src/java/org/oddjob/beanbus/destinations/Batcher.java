package org.oddjob.beanbus.destinations;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusListenerAdapter;

/**
 * Provide batching of beans. Unfinished and untested.
 * 
 * @author rob
 *
 * @param <T>
 */
public class Batcher<T> extends AbstractDestination<T> {

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

	@ArooaHidden
	@Inject
	public void setBeanBus(BusConductor driver) {
		
		driver.addBusListener(new BusListenerAdapter() {
						
			@Override
			public void tripEnding(BusEvent event) throws BusCrashException {
				dispatch();				
			}
			
		});
		
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
