package org.oddjob.beanbus.destinations;

import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusFilter;

import java.util.function.Function;

/**
 * An adaptor from an {@link Function} to a {@link BusFilter}.
 * 
 * @author rob
 *
 * @param <F>
 * @param <T>
 */
public class BusMap<F, T> extends AbstractFilter<F, T>
implements BusFilter<F, T> {

	private Function<? super F, ? extends T> function;

	@Override
	protected T filter(F from) {
		return function.apply(from);
	}
	
		
	public Function<? super F, ? extends T> getFunction() {
		return function;
	}

	public void setFunction(Function<? super F, ? extends T> filter) {
		this.function = filter;
	}	
}
