package org.oddjob.beanbus.destinations;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusFilter;
import org.oddjob.beanbus.TrackingBusListener;

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

	private final List<T> beans = new ArrayList<T>();
	
	private final TrackingBusListener busListener = 
			new TrackingBusListener() {
		@Override
		public void busStarting(BusEvent event) {
			beans.clear();
		}
	};
	
	@Override
	protected T filter(T from) {
		beans.add(from);
		return from;
	}
	
	@ArooaHidden
	@Inject
	public void setBusConductor(BusConductor busConductor) {
		busListener.setBusConductor(busConductor);
	}

	public List<T> getBeans() {
		return beans;
	}
	
	@Override
	public boolean isEmpty() {
		return beans.size() == 0;
	}
	
	public int getCount() {
		return beans.size();
	}
}
