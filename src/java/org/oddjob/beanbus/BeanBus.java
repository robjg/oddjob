package org.oddjob.beanbus;

public interface BeanBus<T> {

	public void startBus() throws BusCrashException;
	
	public void accept(T bean) throws BusCrashException;
	
	public void stopBus() throws BusCrashException;
	
	
}
