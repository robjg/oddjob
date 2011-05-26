package org.oddjob.jmx.server;

import org.oddjob.jmx.RemoteOperation;

/**
 * A definition of a {@link RemoteOperation} based on a
 * MBeanOperaion signature.
 *   
 * @author rob
 *
 */
public class MBeanOperation extends RemoteOperation<Object> {

	private final String actionName;
	
	private final String[] signature;
	
	public MBeanOperation(String actionName, String[] signature) {
		this.actionName = actionName;
		this.signature = signature;
	}
		
	public String getActionName() {
		return actionName;
	}
	
	public String[] getSignature() {
		return signature;
	}
}
