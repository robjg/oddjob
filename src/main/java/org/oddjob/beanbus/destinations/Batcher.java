package org.oddjob.beanbus.destinations;

import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

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
implements BusFilter<T, Collection<T>> {

	private static final Logger logger = LoggerFactory.getLogger(Batcher.class);
	
	private String name;
	
	private int batchSize;

	private Consumer<? super Collection<T>> to;
	
	private volatile List<T> batch;
	
	private int count;

	private BusConductor busConductor;
	
	private final TrackingBusListener busListener = 
			new TrackingBusListener() {
		
		@Override
		public void busStarting(BusEvent event) {
			count = 0;
		}
		
		@Override
		public void tripBeginning(BusEvent event) {
			batch = new ArrayList<>();
		}
		
		@Override
		public void tripEnding(BusEvent event) {
			dispatch();				
		}
	};

	@Override
	public void accept(T bean) {

		batch.add(bean);
		++count;
		
		if (batch.size() == batchSize) {
			try {
				busConductor.cleanBus();
			} catch (BusCrashException e) {
				throw new RuntimeException(e);
			}
		}
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
			logger.info("Dispatching batch of " + batchSize +
					" beans.");
			
			to.accept(batch);
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

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	public Consumer<? super Collection<T>> getTo() {
		return to;
	}

	@Override
	public void setTo(Consumer<? super Collection<T>> next) {
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
