package org.oddjob.jmx.server;

public class MockServerInterfaceManagerFactory 
implements ServerInterfaceManagerFactory {

	public ServerInterfaceManager create(Object target,
			ServerSideToolkit serverSideToolkit) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
