package org.oddjob.beanbus;

import java.util.List;

/**
 * Provide batching of beans. Unfinished and untested.
 * 
 * @author rob
 *
 * @param <T>
 */
public class Batcher<T> implements Destination<T>, BusAware {

	private int batchSize;

	private Destination<? super Iterable<T>> next;
	
	private List<T> batch;
	
	private int count;
	
	private BadBeanHandler<List<? super T>> badBeanHandler;
	
	public void accept(T bean) throws BusCrashException {	

		batch.add(bean);
		if (++count == batchSize) {
			dispatch();
		}
	}
	
	protected void dispatch() throws BusCrashException {
		if (count == 0) {
			return;
		}
		count = 0;

		try {
			next.accept(batch);
		}
		catch (BadBeanException e) {
			if (badBeanHandler == null) {
				throw new BusCrashException("No Bad Bean Handler.", e);				
			}
			else {
				badBeanHandler.handle(batch, e);
			}
		}
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
	
	public Destination<? super Iterable<T>> getNext() {
		return next;
	}

	public void setNext(Destination<? super Iterable<T>> next) {
		this.next = next;
	}

	public BadBeanHandler<List<? super T>> getBadBeanHandler() {
		return badBeanHandler;
	}

	public void setBadBeanHandler(BadBeanHandler<List<? super T>> badBeanHandler) {
		this.badBeanHandler = badBeanHandler;
	}
	
}
