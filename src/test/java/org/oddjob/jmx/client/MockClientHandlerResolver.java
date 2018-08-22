package org.oddjob.jmx.client;

import org.oddjob.arooa.ClassResolver;

public class MockClientHandlerResolver<T> implements ClientHandlerResolver<T> {
	private static final long serialVersionUID = 2009090500L;

	public ClientInterfaceHandlerFactory<T> resolve(ClassResolver classResolver) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
