package org.oddjob.beanbus;

public interface BeanBus<T> extends Destination<T> {

	public void startBus() throws BusCrashException;
	
	public void stopBus() throws BusCrashException;
	
	
}
