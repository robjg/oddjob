/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.MethodOperation;
import org.oddjob.remote.NoSuchOperationException;
import org.oddjob.remote.RemoteException;
import org.oddjob.remote.RemoteInvocationException;

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

	private final long remoteId;

	/** The object operations will be invoked on. */
	private final Object target;
	
	private final Map<RemoteOperation<?>, Method> methods =
			new HashMap<>();
	
	/**
	 * Constructor.
	 * 
	 * @param target The object operations will be invoked on.
	 */
	public ServerAllOperationsHandler(Class<T> cl, T target, long remoteId) {
		this.remoteId = remoteId;
		this.target = target;

		for (Method m : cl.getMethods()) {
			methods.put(MethodOperation.from(m), m);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.jmx.server.ServerInterfaceHandler#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public Object invoke(RemoteOperation<?> operation, Object[] params)
			throws RemoteException {
		
		Method m = methods.get(operation);

		if (m == null) {
			throw NoSuchOperationException.of(
					remoteId, operation.toString(), operation.getSignature());
		}
		
		try {
			return m.invoke(target, params);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw RemoteInvocationException.of(
					remoteId, operation.toString(), operation.getSignature(), params, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.oddjob.jmx.server.ServerInterfaceHandler#destroy()
	 */
	public void destroy() {
	}
}
