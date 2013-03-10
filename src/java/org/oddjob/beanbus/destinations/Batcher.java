package org.oddjob.beanbus.destinations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusFilter;
import org.oddjob.beanbus.TrackingBusListener;

/**
 * @oddjob.description Provide batching of beans.
 * 
 * @oddjob.example
 * 
 * Create Batches of 2 beans.
 * 
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/BatcherExample.xml}
 * 
 * @author rob
 *
 * @param <T> The type of bean being batched.
 */
public class Batcher<T> extends AbstractDestination<T>
implements BusFilter<T, Iterable<T>> {

	private static final Logger logger = Logger.getLogger(Batcher.class);
	
	private String name;
	
	private int batchSize;

	private Collection<? super Iterable<T>> to;
	
	private volatile List<T> batch;
	
	private int count;

	private BusConductor busConductor;
	
	private final TrackingBusListener busListener = 
			new TrackingBusListener() {
		
		@Override
		public void busStarting(BusEvent event) throws BusCrashException {
			count = 0;
		}
		
		@Override
		public void tripBeginning(BusEvent event) throws BusCrashException {
			batch = new ArrayList<T>();
		}
		
		@Override
		public void tripEnding(BusEvent event) throws BusCrashException {
			dispatch();				
		}
	};
	
	@Override
	public boolean add(T bean) {	

		batch.add(bean);
		++count;
		
		if (batch.size() == batchSize) {
			try {
				busConductor.cleanBus();
			} catch (BusCrashException e) {
				throw new RuntimeException(e);
			}
		}
		
		return true;
	}
	
	/**
	 * Dispatch the beans. Called when a batch is ready of a trip
	 * is ending.
	 */
	protected void dispatch() {
		int batchSize = batch.size();
		
		if (batchSize == 0) {
			return;
		}

		if (to == null) {
			logger.info("Discarding batch of " + batchSize + 
					" beans because there is no destination.");
		}
		else {
			logger.info("Dipatching batch of " + batchSize + 
					" beans.");
			
			to.add(batch);
		}
	}

	@ArooaHidden
	@Inject
	public void setBeanBus(BusConductor busConductor) {
		
		this.busConductor = busConductor;
		
		busListener.setBusConductor(busConductor);
	}
	
	public int getCount() {
		return count;
	}
	
	public int getSize() {
		Collection<?> batch = this.batch;
		if (batch == null) {
			return 0;
		}
		else {
			return batch.size();
		}
	}
	
	@Override
	public boolean isEmpty() {
		return getSize() == 0;
	}
	
	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	public Collection<? super Iterable<T>> getTo() {
		return to;
	}

	@Override
	public void setTo(Collection<? super Iterable<T>> next) {
		this.to = next;
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
