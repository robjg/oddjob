package org.oddjob.beanbus;

/**
 * Something that transforms a bean into a different type.
 * 
 * @author rob
 *
 * @param <F>
 * @param <T>
 */
public interface Transformer<F, T> {

	/**
	 * Transform the bean.
	 * 
	 * @param from
	 * @return
	 */
	T transform(F from);
	
}
