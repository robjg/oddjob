package org.oddjob.jmx.server;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

public class MockServerInterfaceHandlerFactory <X, Y>
implements ServerInterfaceHandlerFactory<X, Y> {

	public Class<Y> clientClass() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ServerInterfaceHandler createServerHandler(X target,
			ServerSideToolkit ojmb) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Class<X> interfaceClass() {
		throw new RuntimeException("Unexpected from " + getClass());
	}	
}
