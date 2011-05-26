package org.oddjob.beanbus;

import java.util.List;

public class Batcher<T> implements Destination<T>, BusAware {

	private int batchSize;

	private Destination<List<T>> next;
	
	private List<T> batch;
	
	private int count;
	
	private BadBeanHandler<List<? super T>> badBeanHandler;
	
	public void accept(T bean) throws CrashBusException {	

		batch.add(bean);
		if (++count == batchSize) {
			dispatch();
		}
	}
	
	protected void dispatch() throws CrashBusException {
		if (count == 0) {
			return;
		}
		count = 0;

		try {
			next.accept(batch);
		}
		catch (BadBeanException e) {
			if (badBeanHandler == null) {
				throw new CrashBusException("No Bad Bean Handler.", e);				
			}
			else {
				badBeanHandler.handle(batch, e);
			}
		}
		batch.clear();
	}

	@Override
	public void setBus(BeanBus driver) {
		
		driver.addBusListener(new BusListener() {
			
			@Override
			public void busStarting(BusEvent event) {
			}
			
			@Override
			public void busStopping(BusEvent event) throws CrashBusException {
				dispatch();				
			}
			
			@Override
			public void busCrashed(BusEvent event, BusException e) {
			}
			
			@Override
			public void busTerminated(BusEvent event) {
				event.getSource().removeBusListener(this);
			}
			
		});
		
		if (next instanceof BusAware) {
			((BusAware) next).setBus(driver);
		}
	}
	
	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	public Destination<List<T>> getNext() {
		return next;
	}

	public void setNext(Destination<List<T>> next) {
		this.next = next;
	}

	public BadBeanHandler<List<? super T>> getBadBeanHandler() {
		return badBeanHandler;
	}

	public void setBadBeanHandler(BadBeanHandler<List<? super T>> badBeanHandler) {
		this.badBeanHandler = badBeanHandler;
	}
	
}
