package org.oddjob.beanbus;

import java.util.Collection;

/**
 * The starting point for something that beans can be dispatched too.
 * 
 * @author rob
 *
 * @param <T>
 */
public interface BeanBus<T> extends Collection<T> {

	public void startBus() throws BusCrashException;
		
	public void stopBus() throws BusCrashException;
	
	
}
