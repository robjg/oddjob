package org.oddjob.jmx;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.registry.ServerId;

import java.lang.reflect.Type;

public class MockRemoteDirectory implements RemoteDirectory {

	@Override
	public ServerId getServerId() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public <T> Iterable<T> getAllByType(Class<T> type) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public String getIdFor(Object bean) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public Object lookup(String path) {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	@Override
	public <T> T lookup(String path, Type required)
			throws ArooaConversionException {
		throw new RuntimeException("Unexpected from " + getClass());
	}

}
