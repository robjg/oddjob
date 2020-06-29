package org.oddjob.jmx.server;

/**
 * Something that is able to provide the 
 * @{link ServerInterfaceHandlerFactory}s that the server
 * will use to handle communication between client and
 * server.
 * 
 * @author rob
 *
 */
public interface HandlerFactoryProvider {

	/**
	 * Provide the factories.
	 * 
	 * @return Array of factories. Never null;
	 */
	ServerInterfaceHandlerFactory<?, ?>[] getHandlerFactories();
}
