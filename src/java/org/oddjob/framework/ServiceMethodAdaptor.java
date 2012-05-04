/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import java.lang.reflect.Method;

import org.oddjob.FailedToStopException;

/**
 * 
 * @author rob
 *
 */
public class ServiceMethodAdaptor implements ServiceAdaptor {

	private final Method startMethod;
	private final Method stopMethod;
	private final Object component;
	
	public ServiceMethodAdaptor(Object component, Method startMethod, Method stopMethod) {
		this.component = component;
		this.startMethod = startMethod;
		this.stopMethod = stopMethod;
	}	
	
	public void start() throws Exception {
		startMethod.invoke(component, new Object[0]);
	}
	
	public void stop() throws FailedToStopException {
		try {
			stopMethod.invoke(component, new Object[0]);
		}
		catch (Exception e) {
			throw new FailedToStopException(
					this, "Service failed to stop.", e);
		}
	}

	public Object getComponent() {
		return component;
	}
	
}
