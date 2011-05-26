package org.oddjob.beanbus;

public interface Destination<T> {

	public void accept(T bean) throws BadBeanException, CrashBusException;
}
