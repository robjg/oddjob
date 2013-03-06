package org.oddjob.beanbus;

import java.util.Collection;

/**
 * A bus component that is a filter in the Pipes and Filters design
 * pattern sense.
 * 
 * @author rob
 *
 * @param <F>
 * @param <T>
 */
public interface BusFilter<F, T> extends Collection<F>, Outbound<T> {
	
}
