package org.oddjob.beanbus;

public class EmptyDestination<T> implements Destination<T>{

	public void accept(T bean) {
		
	};
}
