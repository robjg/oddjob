package org.oddjob.jmx.server;

import org.oddjob.arooa.ArooaSession;

public class MockServerSession implements ServerSession {

	@Override
	public long nameFor(Object object) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public Object objectFor(long objectName) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public long createMBeanFor(Object child, ServerContext childContext) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public void destroy(long childName) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public ArooaSession getArooaSession() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
