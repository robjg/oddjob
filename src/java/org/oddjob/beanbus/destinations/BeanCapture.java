package org.oddjob.beanbus.destinations;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusFilter;
import org.oddjob.beanbus.BusListenerAdapter;

/**
 * A {@link BusFilter} that collects beans in a list.
 * 
 * @author rob
 *
 * @param <T> The type of the beans to be collected.
 */
public class BeanCapture<T> extends AbstractFilter<T, T> {

	private final List<T> beans = new ArrayList<T>();
	
	@Override
	protected T filter(T from) {
		beans.add(from);
		return from;
	}
	
	@Inject
	public void setBeanBus(BusConductor driver) {
		driver.addBusListener(new BusListenerAdapter() {
			@Override
			public void busStarting(BusEvent event) {
				beans.clear();
			}
		});
	}

	public List<T> getBeans() {
		return beans;
	}
	
}
