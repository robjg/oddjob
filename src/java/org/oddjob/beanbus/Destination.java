package org.oddjob.beanbus;

/**
 * Something that can accept a bean.
 * 
 * @author rob
 *
 * @param <T> The type of bean the destination can accept.
 */
public interface Destination<T> {

	public void accept(T bean) throws BadBeanException, CrashBusException;
}
