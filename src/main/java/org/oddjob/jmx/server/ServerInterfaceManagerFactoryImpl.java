/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.jmx.JMXServerJob;
import org.oddjob.jmx.SharedConstants;

import java.io.IOException;
import java.util.*;

/**
 * Simple implementation of a {@link ServerInterfaceManagerFactory}
 * 
 * @author rob
 *
 */
public class ServerInterfaceManagerFactoryImpl
implements ServerInterfaceManagerFactory {
	
	/** Handler factories. */
	private final Set<ServerInterfaceHandlerFactory<?, ?>> serverHandlerFactories = 
		new HashSet<>();
	
	/** Access controller. */
	private OddjobJMXAccessController accessController;
	
	/**
	 * Default Constructor.
	 */
	public ServerInterfaceManagerFactoryImpl() {
		this(SharedConstants.DEFAULT_SERVER_HANDLER_FACTORIES);
	}
	
	/**
	 * Constructor with user defined handlers.
	 * 
	 * @param serverHandlerFactories
	 */
	public ServerInterfaceManagerFactoryImpl(
				ServerInterfaceHandlerFactory<?, ?>[] serverHandlerFactories) {
		this(new HashMap<>(), serverHandlerFactories);
	}
	
	/**
	 * Constructor with environment.
	 * 
	 * @param env
	 * 
	 * @throws IOException
	 */
	public ServerInterfaceManagerFactoryImpl(Map<String, ?> env) {
		this(env, SharedConstants.DEFAULT_SERVER_HANDLER_FACTORIES);
		
	}
		
	/**
	 * Constructor for user defined list of factories.
	 * 
	 * @param env
	 * @param serverHandlerFactories
	 * 
	 * @throws IOException 
	 */
	public ServerInterfaceManagerFactoryImpl(Map<String, ?> env,
			ServerInterfaceHandlerFactory<?, ?>[] serverHandlerFactories) {
		addServerHandlerFactories(serverHandlerFactories);
		
		if (env != null) {
			Object accessFile = env.get(JMXServerJob.ACCESS_FILE_PROPERTY);
			if (accessFile != null) {
				try {
					accessController = new OddjobJMXFileAccessController(accessFile.toString());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	/**
	 * Add extra handlers.
	 * 
	 * @param serverHandlerFactories
	 */
	public void addServerHandlerFactories(ServerInterfaceHandlerFactory<?, ?>[] serverHandlerFactories) {
		if (serverHandlerFactories == null) {
			return;
		}

		for (ServerInterfaceHandlerFactory<?, ?> factory : serverHandlerFactories)
		{
			if (this.serverHandlerFactories.contains(factory)) {
				throw new IllegalArgumentException("Failed registering [" + factory + 
						", it is already registered for class [" + factory.serverClass() +"]");
			}
			this.serverHandlerFactories.add(factory);			
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.jmx.server.ServerInterfaceManagerFactory#create(java.lang.Object, org.oddjob.jmx.server.ServerSideToolkit)
	 */
	public ServerInterfaceManager create(Object target, ServerSideToolkit serverSideToolkit) {
		List<ServerInterfaceHandlerFactory<?, ?>> handlers =
				new ArrayList<>();

		// build up a list of supported interfaces
		for (ServerInterfaceHandlerFactory<?, ?> interfaceHandler : serverHandlerFactories) {
			Class<?> handles = interfaceHandler.serverClass();
			if (handles.isInstance(target)) {
				handlers.add(interfaceHandler);
			}
		}
					
		// create the implementation
		return new ServerInterfaceManagerImpl(
				target,
				serverSideToolkit,
				(ServerInterfaceHandlerFactory[]) handlers.toArray(new ServerInterfaceHandlerFactory[0]),
				accessController);
	}
}
