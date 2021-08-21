package org.oddjob.beanbus.destinations;

import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusFilter;
import org.oddjob.framework.adapt.Start;
import org.oddjob.framework.adapt.Stop;

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
 *  <li>{@link BeanLimit}</li>
 * </ul>
 * 
 * 
 * @author rob
 *
 * @param <T> The type of the beans to be collected.
 */
public class BeanCapture<T> extends AbstractFilter<T, T> implements Runnable, AutoCloseable {

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
