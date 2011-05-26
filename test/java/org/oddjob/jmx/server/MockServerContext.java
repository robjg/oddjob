package org.oddjob.jmx.server;

import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;

public class MockServerContext implements ServerContext {

	public ServerContext addChild(Object child) throws ServerLoopBackException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ServerId getServerId() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ConsoleArchiver getConsoleArchiver() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public LogArchiver getLogArchiver() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ServerModel getModel() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public void removeChild(Object child) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Address getAddress() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public BeanDirectory getBeanDirectory() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
