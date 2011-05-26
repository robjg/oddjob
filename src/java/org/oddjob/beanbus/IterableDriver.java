package org.oddjob.beanbus;

import java.util.Iterator;

public class IterableDriver<T> implements Driver<T>, BusAware {

	private Iterable<T> iterable;
	
	private Destination<? super T> to;

	private volatile boolean stop;
		
	@Override
	public void go() throws BusException {
		
		stop = false;
		
		Iterator<T> current = iterable.iterator();

		while (!stop && current.hasNext()) {
				
			to.accept(current.next());
		}			
	}
	
	@Override
	public void setBus(BeanBus driver) {
		if (to instanceof BusAware) {
			((BusAware) to).setBus(driver);
		}		
	}
	
	public void setTo(Destination<? super T> to) {
		this.to = to;
	};
	
	@Override
	public void stop() {
		this.stop = true;
	}
	
	public Iterable<T> getIterable() {
		return iterable;
	}

	public void setIterable(Iterable<T> iterable) {
		this.iterable = iterable;
	}
}
