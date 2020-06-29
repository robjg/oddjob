package org.oddjob.jmx.client;

import org.oddjob.arooa.ClassResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaHandlerResolver<T> implements ClientHandlerResolver<T> {
	private static final long serialVersionUID = 2009090500L;
	
	private static final Logger logger = LoggerFactory.getLogger(
			VanillaHandlerResolver.class);
	
	private final String className;
	
	public VanillaHandlerResolver(String className) {
		this(className, null);
	}
	
	public VanillaHandlerResolver(String className, String prefix) {
		if (className == null) {
			throw new NullPointerException("Class Name.");
		}
		
		this.className = className;
	}
	
	public String getClassName() {
		return className;
	}
	
	@SuppressWarnings("unchecked")
	public ClientInterfaceHandlerFactory<T> resolve(ClassResolver classResolver) {
		
		Class<T> cl = (Class<T>) classResolver.findClass(className);
		
		if (cl == null) {
			logger.info("No Class for " + className);
			return null;
		}
		
		return new DirectInvocationClientFactory<>(cl);
	}
}
