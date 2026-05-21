package org.oddjob;

import org.oddjob.input.InputHandler;

import java.lang.reflect.Type;

public class MockOddjobServices implements OddjobServices {

	public Object getService(String serviceName) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public String serviceNameFor(Type theClass, String flavour) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public ClassLoader getClassLoader() {
		throw new RuntimeException("Unexpected from " + getClass());
	}	
	
	@Override
	public OddjobExecutors getOddjobExecutors() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public InputHandler getInputHandler() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
