package org.oddjob.persist;

import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.registry.Path;

public class MockPersisterBase extends PersisterBase {

	@Override
	protected void clear(Path path) {
		throw new RuntimeException("Unsupported from " + getClass());
	}

	@Override
	protected void persist(Path path, String id, Object component) {
		throw new RuntimeException("Unsupported from " + getClass());
	}

	@Override
	protected String[] list(Path path)
			throws ComponentPersistException {
		throw new RuntimeException("Unsupported from " + getClass());
	}
	
	@Override
	protected void remove(Path path, String id) {
		throw new RuntimeException("Unsupported from " + getClass());
	}

	@Override
	protected Object restore(Path path, String id, ClassLoader classLoader) {
		throw new RuntimeException("Unsupported from " + getClass());
	}
}
