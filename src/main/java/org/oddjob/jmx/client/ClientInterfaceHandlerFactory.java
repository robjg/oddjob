/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;


import org.oddjob.remote.Initialisation;

/**
 * Implementations are able to create a handler for the client
 * side of method invocations.
 *
 * @author Rob Gordon.
 *
 * @param <T> The Type of the interface this will creating a handler for.
 * 
 */
public interface ClientInterfaceHandlerFactory<T> {

	/**
	 * The version of this handler.
	 * 
	 * @return
	 */
	HandlerVersion getVersion();
	
	/**
	 * Provide the interface class this is the information
	 * about.
	 * 
	 * @return The class.
	 */
	Class<T> interfaceClass();
		
	
	/**
	 * Create a thing that handles communication 
	 * with the server for an Interface..
	 *  
	 * @param proxy The client side proxy the invocations are coming from. Most of the
	 * time this will be ignored but it's useful as the source for events.
	 * 
	 * @param toolkit Tools to help the handler do it's job.
	 * 
	 * @return An Handler object. Never null.
	 */
	default T createClientHandler(T proxy, ClientSideToolkit toolkit) {
		throw new UnsupportedOperationException("Initialisation Data required for Handler for " +
				interfaceClass().getName());
	}

	default T createClientHandler(T proxy, ClientSideToolkit toolkit,
								  Initialisation<?> initialisation) {
		throw new UnsupportedOperationException("Initialisation Data not expected for Handler for " +
				interfaceClass().getName());
	}

	
}
