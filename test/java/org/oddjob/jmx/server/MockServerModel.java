package org.oddjob.jmx.server;

import org.oddjob.arooa.registry.ServerId;
import org.oddjob.util.ThreadManager;

public class MockServerModel implements ServerModel{

	public ServerInterfaceManagerFactory getInterfaceManagerFactory() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public String getLogFormat() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ThreadManager getThreadManager() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ServerId getServerId() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
