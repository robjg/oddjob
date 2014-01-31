/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import java.beans.ExceptionListener;
import java.lang.reflect.Method;

import org.oddjob.FailedToStopException;

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
	 * @param acceptStopHandleMethod
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
	
	@Override
	public void acceptExceptionListener(ExceptionListener exceptionListener) {
		if (acceptExceptionListenerMethod != null) {
			try {
				acceptExceptionListenerMethod.invoke(component, 
						new Object[] { exceptionListener });
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
