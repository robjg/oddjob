package org.oddjob.jmx.server;

import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.ReflectionException;

import org.oddjob.jmx.RemoteOperation;

public class MockServerInterfaceHandler implements ServerInterfaceHandler {

	public Object invoke(RemoteOperation<?> operation, Object[] params)
			throws MBeanException, ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	
	public Notification[] getLastNotifications() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void destroy() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	

	
}
