package org.oddjob.beanbus.destinations;

import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link BusFilter} that collects beans in a list.
 * 
 * @oddjob.example
 * 
 * There are many examples elsewhere.
 * <ul>
 * 	<li>{@link Batcher}</li>
 * 	<li>{@link BeanCopy}</li>
 *  <li>{@link BeanQueue}</li>
 *  <li>{@link OnlyFilter}</li>
 * </ul>
 * 
 * 
 * @author rob
 *
 * @param <T> The type of the beans to be collected.
 */
public class BeanCapture<T> extends AbstractFilter<T, T> {

	private final List<T> beans = new ArrayList<>();
	
	private final TrackingBusListener busListener = 
			new TrackingBusListener() {
		@Override
		public void busStarting(BusEvent event) {
			beans.clear();
		}
	};
	
	@Override
	protected T filter(T from) {
		synchronized(beans) {
			beans.add(from);
		}
		return from;
	}
	
	@ArooaHidden
	@Inject
	public void setBusConductor(BusConductor busConductor) {
		busListener.setBusConductor(busConductor);
	}

	public List<T> getBeans() {
		synchronized (beans) {
			return beans;
		}
	}

	public int getCount() {
		synchronized (beans) {
			return beans.size();
		}
	}
}
