package org.oddjob.jmx.client;

import java.lang.reflect.Method;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.Utils;

/**
 * A {@link RemoteOperation} created based on a Method.
 * 
 * @author rob
 *
 */
public class MethodOperation extends RemoteOperation<Object> {

	private final String actionName;
	
	private final String[] signature;

	public MethodOperation(Method method) {
		actionName = method.getName();
		signature = Utils.classArray2StringArray(method.getParameterTypes());
	}
	
	public String getActionName() {
		return actionName;
	}
	
	public String[] getSignature() {
		return signature;
	}
	
	
}
