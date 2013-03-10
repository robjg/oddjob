package org.oddjob.beanbus.destinations;

import org.oddjob.beanbus.AbstractFilter;

/**
 * @oddjob.description Allows the flow of beans to be turned on and off.
 * 
 * @author rob
 *
 * @param <T> 
 */
public class ValveFilter<T> extends AbstractFilter<T, T>{

	private volatile boolean open;
	
	@Override
	protected T filter(T from) {
		if (open) {
			return from;
		}
		else {
			return null;
		}
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
	
	
}
