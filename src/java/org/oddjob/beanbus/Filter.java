package org.oddjob.beanbus;

public interface Filter<F, T> {

	public T filter(F from);
	
}
