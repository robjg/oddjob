package org.oddjob.beanbus;

public class EmptyDestination<T> extends AbstractDestination<T> {

	@Override
	public boolean add(T bean) {
		return false;
	};
}
