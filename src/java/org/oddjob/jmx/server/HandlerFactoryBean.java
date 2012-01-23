package org.oddjob.jmx.server;

/**
 * A {@link HandlerFactoryProvider} to be used in a configuration file.
 * 
 * @author rob
 *
 */
public class HandlerFactoryBean implements HandlerFactoryProvider {

	/** The Handler Factories property */
	private ServerInterfaceHandlerFactory<?, ?>[] handlerFactories;
	
	public ServerInterfaceHandlerFactory<?, ?>[] getHandlerFactories() {
		return handlerFactories;
	}
	
	/**
	 * Setter.
	 * 
	 * @param serverHandlers
	 */
	public void setHandlerFactories(ServerInterfaceHandlerFactory<?, ?>[] serverHandlers) {
		this.handlerFactories = serverHandlers;
	}
}
