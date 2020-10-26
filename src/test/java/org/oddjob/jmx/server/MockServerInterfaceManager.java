package org.oddjob.jmx.server;

import org.oddjob.remote.Implementation;

import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

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
	public Implementation<?>[] allClientInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
