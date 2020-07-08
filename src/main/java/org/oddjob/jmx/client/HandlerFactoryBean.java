package org.oddjob.jmx.client;

/**
 * A {@link HandlerFactoryProvider} to be used in a configuration file.
 * 
 * @author rob
 *
 */
public class HandlerFactoryBean implements HandlerFactoryProvider {

	/** The Handler Factories property */
	private ClientInterfaceHandlerFactory<?>[] handlerFactories;
	
	public ClientInterfaceHandlerFactory<?>[] getHandlerFactories() {
		return handlerFactories;
	}
	
	/**
	 * Setter.
	 * 
	 * @param clientHandlers
	 */
	public void setHandlerFactories(ClientInterfaceHandlerFactory<?>[] clientHandlers) {
		this.handlerFactories = clientHandlers;
	}
}
