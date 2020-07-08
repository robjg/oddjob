package org.oddjob.jmx.client;

import org.oddjob.arooa.ArooaSession;
import org.slf4j.Logger;

public class MockClientSession implements ClientSession {

	@Override
	public Object create(long objectName) {
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
	public long nameFor(Object object) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public Object objectFor(long objectName) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public Logger logger() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public void destroyAll() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public ClientInterfaceManagerFactory getInterfaceManagerFactory() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
