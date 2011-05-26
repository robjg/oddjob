package org.oddjob.jmx.server;

public class HandlerFactoryBean implements HandlerFactoryProvider {

	private ServerInterfaceHandlerFactory<?, ?>[] handlerFactories;
	
	public ServerInterfaceHandlerFactory<?, ?>[] getHandlerFactories() {
		return handlerFactories;
	}
	
	public void setHandlerFactories(ServerInterfaceHandlerFactory<?, ?>[] serverHandlers) {
		this.handlerFactories = serverHandlers;
	}
}
