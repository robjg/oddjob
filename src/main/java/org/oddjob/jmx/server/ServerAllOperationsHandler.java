/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.MethodOperation;

import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle invoking operation on a target object. Essentially
 * provides a translation from JMX style argument to 
 * Method.
 * 
 * @author rob
 *
 */
public class ServerAllOperationsHandler<T> implements ServerInterfaceHandler {

	/** The object operations will be invoked on. */
	private final Object target;
	
	private final Map<RemoteOperation<?>, Method> methods =
			new HashMap<>();
	
	/**
	 * Constructor.
	 * 
	 * @param target The object operations will be invoked on.
	 */
	public ServerAllOperationsHandler(Class<T> cl, T target) {
		this.target = target;

		for (Method m : cl.getMethods()) {
			methods.put(new MethodOperation(m), m);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.jmx.server.ServerInterfaceHandler#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public Object invoke(RemoteOperation<?> operation, Object[] params) throws MBeanException, ReflectionException {
		
		Method m = methods.get(operation);

		if (m == null) {
			throw new ReflectionException(new NoSuchMethodException(operation.toString()));
		}
		
		try {
			return m.invoke(target, params);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e1) {
			throw new ReflectionException(e1, operation.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.oddjob.jmx.server.ServerInterfaceHandler#destroy()
	 */
	public void destroy() {
	}
}
