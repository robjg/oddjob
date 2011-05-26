/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import java.lang.reflect.Method;

public class Service {

	private final Method startMethod;
	private final Method stopMethod;
	private final Object component;
	
	private Service(Object component, Method startMethod, Method stopMethod) {
		this.component = component;
		this.startMethod = startMethod;
		this.stopMethod = stopMethod;
	}	
	
	public void start() throws Exception {
		startMethod.invoke(component, new Object[0]);
	}
	
	public void stop() throws Exception {
		stopMethod.invoke(component, new Object[0]);
	}

	public Object getComponent() {
		return component;
	}
	
	public static Service serviceFor(Object component) {
		Class<?> cl = component.getClass();
		try {
			Method startMethod = cl.getDeclaredMethod("start", new Class[0]);
			if (startMethod.getReturnType() != Void.TYPE) {
				return null;
			}
			Method stopMethod = cl.getDeclaredMethod("stop", new Class[0]);
			if (startMethod.getReturnType() != Void.TYPE) {
				return null;
			}
			return new Service(component, startMethod, stopMethod);
		} catch (Exception e) {
			return null;			
		}
	}
}
