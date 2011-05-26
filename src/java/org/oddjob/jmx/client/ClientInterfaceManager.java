package org.oddjob.jmx.client;

import java.lang.reflect.Method;

public interface ClientInterfaceManager {

	
	public Object invoke(Method m, Object[] args)
	throws Throwable;
}
