package org.oddjob.beanbus.destinations;

import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BusFilter;

import java.util.function.Predicate;

/**
 * An adaptor from an {@code java.util.function.Predicate} to a {@link BusFilter}.
 * 
 * @author rob
 *
 * @param <T>
 */
public class BeanFilter<T> extends AbstractFilter<T, T>
implements BusFilter<T, T> {

	private Predicate<? super T> predicate;

	@Override
	protected T filter(T from) {
		if (predicate.test(from)) {
			return from;
		}
		else {
			return null;
		}
	}
	
		
	public Predicate<? super T> getPredicate() {
		return predicate;
	}

	public void setPredicate(Predicate<? super T> filter) {
		this.predicate = filter;
	}	
}
