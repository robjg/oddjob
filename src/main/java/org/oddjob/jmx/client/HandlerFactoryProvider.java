package org.oddjob.jmx.client;

/**
 * Something that is able to provide the 
 * {@link ClientInterfaceHandlerFactory}s that the client
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
	ClientInterfaceHandlerFactory<?>[] getHandlerFactories();
}
