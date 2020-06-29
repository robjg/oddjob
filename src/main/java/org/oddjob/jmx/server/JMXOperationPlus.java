package org.oddjob.jmx.server;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.util.ArrayList;
import java.util.List;

public class JMXOperationPlus<T> extends JMXOperation<T> {

	private final String actionName;
		
	private final Class<T> returnType;
	
	private final String description; 
	
	private final int impact;
	
	private final List<Param> params = new ArrayList<>();
	
	public JMXOperationPlus(String actionName, 
			String description,
			Class<T> returnType,
			int impact) {
			
		this.actionName = actionName;
		this.returnType = returnType;
		this.description = description;
		this.impact = impact;
	}
			
	public String getActionName() {
		return actionName;
	}
		
	public String[] getSignature() {
		String[] signature = new String[params.size()];
		int i = 0;
		for (Param param : params) {
			signature[i++] = param.type.getName();
		}
		return signature;
	}

	public MBeanOperationInfo getOpInfo() {
		MBeanParameterInfo[] paramInfos = new MBeanParameterInfo[params.size()];		
		int i = 0;
		for (Param param : params) {
			paramInfos[i++] = new MBeanParameterInfo(
					param.name, param.type.getName(), param.description);
		}
		return new MBeanOperationInfo(
				actionName, 
				description, 
				paramInfos, 
				returnType.getName(), 
				impact);
	}

	public JMXOperationPlus<T> addParam(String name, Class<?> type, String description) {
		params.add(new Param(name, type, description));
		return this;
	}

	static class Param {
		final String name;
		final Class<?> type;
		final String description;
		
		Param(String name, Class<?> type, String description) {
			this.name = name;
			this.type = type;
			this.description = description;
		}
	}
		
}
