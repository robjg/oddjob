package org.oddjob.beanbus;

public interface Driver<T> {

	public void setTo(Destination<? super T> to);
	
	public void go() throws BusException;
	
	public void stop();
}
