package org.oddjob.beanbus;

import java.util.function.Consumer;

/**
 * The starting point for something that beans can be dispatched too.
 * 
 * @author rob
 *
 * @param <T>
 */
public interface BeanBus<T> extends Consumer<T> {

	void startBus() throws BusCrashException;
		
	void stopBus() throws BusCrashException;
	
	
}
