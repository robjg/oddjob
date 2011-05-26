package org.oddjob.jmx;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.registry.ServerId;

public class MockRemoteDirectory implements RemoteDirectory {

	public ServerId getServerId() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public <T> Iterable<T> getAllByType(Class<T> type) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public String getIdFor(Object bean) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public Object lookup(String path) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public <T> T lookup(String path, Class<T> required)
			throws ArooaConversionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
