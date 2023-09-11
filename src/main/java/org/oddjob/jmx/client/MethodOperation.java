package org.oddjob.jmx.client;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.Utils;
import org.oddjob.remote.HasOperationType;
import org.oddjob.remote.OperationType;

import java.lang.reflect.Method;

/**
 * A {@link RemoteOperation} created based on a Method.
 * 
 * @author rob
 *
 */
public class MethodOperation<T> extends RemoteOperation<T> implements HasOperationType<T> {

	private final String[] signature;

	private final OperationType<T> operationType;

	private MethodOperation(OperationType<T> operationType) {
		this.operationType = operationType;
		signature = Utils.classArray2StringArray(operationType.getSignature());
	}

	@SuppressWarnings("unchecked")
	public static <T> MethodOperation<T> from(Method method) {

		return new MethodOperation<>(
				new OperationType<>(method.getName(),
						method.getParameterTypes(),
						(Class<T>) method.getReturnType()));
	}


	public String getActionName() {
		return operationType.getName();
	}
	
	public String[] getSignature() {
		return signature;
	}

	@Override
	public OperationType<T> getOperationType() {
		return operationType;
	}
}
