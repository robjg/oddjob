package org.oddjob.jmx.server;

import org.oddjob.jmx.RemoteOperation;

import javax.management.MBeanOperationInfo;
import java.lang.reflect.Method;

/**
 * A {@link RemoteOperation} created based on a Method.
 * 
 * @author rob
 *
 */
public class JMXOperationFactory  {

	private final Class<?> cl;
	
	public JMXOperationFactory(Class<?> cl) {
		this.cl = cl;
	}
		
	public <T> JMXOperation<T> operationFor(
			String methodName,
			int impact) {
		return operationFor(methodName, null, impact);
	}
	
	public <T> JMXOperation<T> operationFor(
			String methodName, 
			String description, 
			int impact, 
			Class<?>... args) {
		
		Method method;
		try {
			method = cl.getMethod(methodName, args);
		} catch (SecurityException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}

		return operationFor(method, description, impact);
	}
	
	public <T> JMXOperation<T> operationFor(
			Method method, 
			int impact) {
		return operationFor(method, null, MBeanOperationInfo.UNKNOWN);
	}
	
	@SuppressWarnings("unchecked")
	public <T> JMXOperation<T> operationFor(
			Method method, 
			String description, 
			int impact) {
		
		if (description == null) {
			description = method.getName() + 
				" method of interface " + cl.getName();
		}
		
		Class<T> returnType = (Class<T>) method.getReturnType();
		JMXOperationPlus<T> op = new JMXOperationPlus<>(
				method.getName(), description, returnType, impact);
		int i = 0;
		for (Class<?> arg : method.getParameterTypes()) {
			op = op.addParam("arg" + i++, arg, "");
		}
		
		return op;
	}
}
