package org.oddjob.jmx.client;

import java.lang.reflect.Method;

/**
 * Dispatches methods to the correct handlers.
 * 
 * @author rob
 *
 */
public interface ClientInterfaceManager extends Destroyable {

	/**
	 * Invoke a method.
	 * 
	 * @param method
	 * @param args
	 * @return
	 * 
	 * @throws Throwable
	 */
	public Object invoke(Method method, Object[] args)
	throws Throwable;
}
