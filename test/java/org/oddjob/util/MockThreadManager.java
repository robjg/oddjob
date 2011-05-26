package org.oddjob.util;

public class MockThreadManager implements ThreadManager {

	public String[] activeDescriptions() {
		throw new RuntimeException("Unexpected form " + getClass());
	}

	public ClassLoader getClassLoader() {
		throw new RuntimeException("Unexpected form " + getClass());
	}

	public void run(Runnable runnable, String description) {
		throw new RuntimeException("Unexpected form " + getClass());
	}

	public void setClassLoader(ClassLoader classLoader) {
		throw new RuntimeException("Unexpected form " + getClass());
	}

	public void close() {
		throw new RuntimeException("Unexpected form " + getClass());
	}

}
