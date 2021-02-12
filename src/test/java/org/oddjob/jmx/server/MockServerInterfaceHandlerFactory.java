package org.oddjob.jmx.server;

import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.remote.NotificationType;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import java.util.List;

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
	public List<NotificationType<?>> getNotificationTypes() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public MBeanOperationInfo[] getMBeanOperationInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
