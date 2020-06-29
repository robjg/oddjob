package org.oddjob.jmx.client;

import org.oddjob.arooa.ClassResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple {@link ClientHandlerResolver}.
 * <p>
 * This implementation will use the remote version to decide whether to
 * resolve a client interface handler factory. If only the minor version 
 * differs a warning is issued, if a major version differs the
 * handler is not resolved.
 * 
 * 
 * @author rob
 *
 * @param <T> The type of the {@link ClientInterfaceHandlerFactory}.
 */
public class SimpleHandlerResolver<T> implements ClientHandlerResolver<T> {
	private static final long serialVersionUID = 2009090500L;
	
	private static final Logger logger = LoggerFactory.getLogger(
			SimpleHandlerResolver.class);
	
	/** The client interface handler factory class name. */
	private final String className;
	
	/** The remote version. */
	private final HandlerVersion remoteVersion;

	/**
	 * Constructor.
	 * 
	 * @param className The name of the {@link ClientInterfaceHandlerFactory}.
	 * @param version The version. The remote version. This is set
	 * by the server before this class comes across the wire.
	 */
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
	
	/**
	 * Get the name of the {@link ClientInterfaceHandlerFactory}.
	 *  
	 * @return The class name.
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Get the server version of this .
	 * 
	 * @return
	 */
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
		
		ClientInterfaceHandlerFactory<T> factory;
		try {
			factory = cl.newInstance();
		} catch (Exception e) {
			logger.error("Failed to instantiate ClientHandlerFactory for " + 
					className, e);
			return null;
		}
		
		HandlerVersion localVersion = factory.getVersion();

		if (remoteVersion.getMajor() != localVersion.getMajor()) {
			logger.warn("Major local version " + 
					localVersion.getVersionAsText() +
					" does not match major remote version " + 
					remoteVersion.getVersionAsText() + 
					" of Handler " + className + "\n" + 
					"Handler for " + factory.interfaceClass().getName() + 
					" will not be implemented.");
			return null;
		}
		if (remoteVersion.getMinor() != localVersion.getMinor()) {
			logger.info("Minor local version " + 
					localVersion.getVersionAsText() +
					" does not match minor remote version " + 
					remoteVersion.getVersionAsText() + 
					" of Handler " + className + "\n" +
					"Minor version differences acceptable for handler for " +
					factory.interfaceClass().getName() + 
					" so continuing to resolve handler.");
		}
		
		return factory;
	}
}
