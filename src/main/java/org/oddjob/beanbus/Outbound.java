package org.oddjob.beanbus;

import java.util.Collection;

/**
 * 
 * A bus component that has a single standard destination.
 * 
 * @author rob
 *
 * @param <T>
 */
public interface Outbound<T> {

	/**
	 * Set the out bound destination.
	 * 
	 * @param to
	 */
	public void setTo(Collection<? super T> destination);

}
