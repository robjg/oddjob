package org.oddjob.jmx.client;

public class MockClientInterfaceHandlerFactory<T> 
implements ClientInterfaceHandlerFactory<T> {
	
	public T createClientHandler(T proxy,
			ClientSideToolkit toolkit) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public HandlerVersion getVersion() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public Class<T> interfaceClass() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
