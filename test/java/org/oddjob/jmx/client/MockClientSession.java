package org.oddjob.jmx.client;

import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaSession;

public class MockClientSession implements ClientSession {

	@Override
	public Object create(ObjectName objectName) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public void destroy(Object proxy) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public ArooaSession getArooaSession() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public ObjectName nameFor(Object object) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public Object objectFor(ObjectName objectName) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public Logger logger() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
