package org.oddjob.beanbus;

import java.util.Collection;

/**
 * A bus component that is a filter in the Pipes and Filters design
 * pattern sense.
 * 
 * @author rob
 *
 * @param <F> The from type.
 * @param <T> The to type.
 */
public interface BusFilter<F, T> extends Collection<F>, Outbound<T> {
	
}
