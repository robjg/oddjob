package org.oddjob.jmx.server;

import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.Notification;
import javax.management.ReflectionException;

import org.oddjob.jmx.client.ClientHandlerResolver;

public class MockServerInterfaceManager 
implements ServerInterfaceManager {

	public void destroy() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Notification[] getLastNotifications() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public MBeanInfo getMBeanInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ClientHandlerResolver<?>[] allClientInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
