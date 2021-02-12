package org.oddjob.jmx.server;

import org.oddjob.remote.Implementation;
import org.oddjob.remote.NotificationType;

import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import java.util.Set;

public class MockServerInterfaceManager 
implements ServerInterfaceManager {

	@Override
	public void destroy() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public Set<NotificationType<?>> getNotificationTypes() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public Implementation<?>[] allClientInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
