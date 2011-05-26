package org.oddjob.jmx;

import org.oddjob.jmx.server.ServerInfo;

public class MockRemoteOddjobBean implements RemoteOddjobBean {

	public void noop() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public ServerInfo serverInfo() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
