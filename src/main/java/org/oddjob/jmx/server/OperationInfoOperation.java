package org.oddjob.jmx.server;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.oddjob.jmx.RemoteOperation;

/**
 * A definition of a {@link RemoteOperation} base on
 * JMX MBeanOperationInfo.
 * 
 * @author rob
 *
 */
public class OperationInfoOperation extends JMXOperation<Object> {

	private final String actionName;
	
	private final String[] signature;
	
	private final MBeanOperationInfo opInfo;
	
	public OperationInfoOperation(MBeanOperationInfo opInfo) {
		this.actionName = opInfo.getName();
		
		signature = new String[opInfo.getSignature().length];
		int i = 0;
		for (MBeanParameterInfo param: opInfo.getSignature()) {
			signature[i++] = param.getType();
		}

		this.opInfo = opInfo;
	}
		
	public String getActionName() {
		return actionName;
	}
	
	public String[] getSignature() {
		return signature;
	}

	public MBeanOperationInfo getOpInfo() {
		return opInfo;
	}
}
