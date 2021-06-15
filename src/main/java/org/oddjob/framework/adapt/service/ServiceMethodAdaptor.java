/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework.adapt.service;

import org.oddjob.FailedToStopException;

import java.beans.ExceptionListener;
import java.lang.reflect.Method;

/**
 * A {@link ServiceAdaptor} that uses a start and stop method.
 * 
 * @author rob
 *
 */
public class ServiceMethodAdaptor implements ServiceAdaptor {

	private final Method startMethod;
	private final Method stopMethod;
	private final Object component;
	
	private final Method acceptExceptionListenerMethod;
	
	/**
	 * Create a new instance.
	 * 
	 * @param component
	 * @param startMethod
	 * @param stopMethod
	 */
	public ServiceMethodAdaptor(Object component, 
			Method startMethod, Method stopMethod) {
		this(component, startMethod ,stopMethod, null);
	}	
	
	/**
	 * Create a new instance with stop handle and exception listener.
	 * 
	 * @param component
	 * @param startMethod
	 * @param stopMethod
	 * @param acceptExceptionListenerMethod
	 */
	public ServiceMethodAdaptor(Object component, 
			Method startMethod, Method stopMethod,
			Method acceptExceptionListenerMethod) {
		this.component = component;
		this.startMethod = startMethod;
		this.stopMethod = stopMethod;
		this.acceptExceptionListenerMethod = acceptExceptionListenerMethod;
	}	
	
	public void start() throws Exception {
		startMethod.invoke(component);
	}
	
	public void stop() throws FailedToStopException {
		try {
			stopMethod.invoke(component);
		}
		catch (Exception e) {
			throw new FailedToStopException(
					this, "Service failed to stop.", e);
		}
	}
	
	@Override
	public void acceptExceptionListener(ExceptionListener exceptionListener) {
		if (acceptExceptionListenerMethod != null) {
			try {
				acceptExceptionListenerMethod.invoke(component, exceptionListener);
			}
			catch (RuntimeException e) {
				throw e;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public Object getComponent() {
		return component;
	}
	
}
