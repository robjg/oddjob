package org.oddjob.jmx.client;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ClassResolver;

public class SimpleHandlerResolver<T> implements ClientHandlerResolver<T> {
	private static final long serialVersionUID = 2009090500L;
	
	private static final Logger logger = Logger.getLogger(
			SimpleHandlerResolver.class);
	
	private final String className;
	
	private final HandlerVersion remoteVersion;

	public SimpleHandlerResolver(String className, HandlerVersion version) {
		if (className == null) {
			throw new NullPointerException("Class Name.");
		}
		if (version == null) {
			throw new NullPointerException("Version.");
		}
		
		this.className = className;
		this.remoteVersion = version;
	}
	
	public String getClassName() {
		return className;
	}
	
	public HandlerVersion getRemoteVersion() {
		return remoteVersion;
	}
	
	@SuppressWarnings("unchecked")
	public ClientInterfaceHandlerFactory<T> resolve(ClassResolver classResolver) {
		
		Class<ClientInterfaceHandlerFactory<T>> cl = 
			(Class<ClientInterfaceHandlerFactory<T>>) classResolver.findClass(className);
		
		if (cl == null) {
			logger.info("No ClientHandlerFactory for " + className);
			return null;
		}
		
		ClientInterfaceHandlerFactory<T> factory = null;
		try {
			factory = cl.newInstance();
		} catch (Exception e) {
			logger.error("Failed to instantiate ClientHandlerFactory for " + 
					className, e);
			return null;
		}
		
		HandlerVersion localVersion = factory.getVersion();

		if (remoteVersion.getMajor() != localVersion.getMajor()) {
			logger.info("Major local version " + localVersion.getMajor() +
					" does not match remote version " + 
					remoteVersion.getMajor() + 
					" of Handler " + className);
			logger.info("Handler " + className + 
					" will not be implemented.");
			return null;
		}
		if (remoteVersion.getMinor() != localVersion.getMinor()) {
			logger.info("Minor local version " + localVersion.getMinor() +
					" does not match remote version " + 
					remoteVersion.getMinor() + 
					" of Handler " + className);
			logger.info("Minor version differences acceptable so continuing with handler." 
					);
		}
		
		return factory;
	}
}
