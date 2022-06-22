package org.oddjob.beanbus.destinations;

import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.framework.adapt.Start;
import org.oddjob.framework.adapt.Stop;

import java.util.ArrayList;
import java.util.List;

/**
 * @oddjob.description A component that collects beans in a list. Additionally, this component may
 * be used in the middle of a {@link org.oddjob.beanbus.bus.BasicBusService} so can act as a Wire Tap.
 * 
 * @oddjob.example
 * 
 * There are many examples elsewhere.
 * <ul>
 * 	<li>{@link Batcher}</li>
 * 	<li>{@link BeanCopy}</li>
 *  <li>{@link BusQueue}</li>
 *  <li>{@link BusLimit}</li>
 * </ul>
 * 
 * 
 * @author rob
 *
 * @param <T> The type of the beans to be collected.
 */
public class BeanCapture<T> extends AbstractFilter<T, T> implements Runnable, AutoCloseable {

	/**
	 * @oddjob.property
	 * @oddjob.description The captured beans.
	 * @oddjob.required Read only.
	 */
	private final List<T> beans = new ArrayList<>();

	@Stop
	@Override
	public void close() {

	}

	@Start
	@Override
	public void run() {
		beans.clear();
	}

	@Override
	protected T filter(T from) {
		synchronized(beans) {
			beans.add(from);
		}
		return from;
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
