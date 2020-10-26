package org.oddjob.jmx.server;

import org.oddjob.jmx.client.HandlerVersion;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

public class MockServerInterfaceHandlerFactory <X, Y>
implements ServerInterfaceHandlerFactory<X, Y> {

	@Override
	public Class<Y> clientClass() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public Class<X> serverClass() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public HandlerVersion getHandlerVersion() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public ServerInterfaceHandler createServerHandler(X target,
													  ServerSideToolkit ojmb) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public MBeanOperationInfo[] getMBeanOperationInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
