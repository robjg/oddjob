package org.oddjob.beanbus;

import java.util.function.Consumer;

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
	 * @param destination
	 */
	void setTo(Consumer<? super T> destination);

}
