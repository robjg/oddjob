package org.oddjob.jmx.server;

import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.monitor.context.AncestorContext;

public class MockServerContext implements ServerContext {

	@Override
	public Object getThisComponent() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public AncestorContext getParent() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

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

	public Address getAddress() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public BeanDirectory getBeanDirectory() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
