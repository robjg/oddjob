/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;



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
	public HandlerVersion getVersion();
	
	/**
	 * Provide the interface class this is the information
	 * about.
	 * 
	 * @return The class.
	 */
	public Class<T> interfaceClass();
		
	
	/**
	 * Create a thing that handles communication 
	 * with the server for an Interface..
	 *  
	 * @param proxy The client side proxy the invocations are coming from. Most of the
	 * time this will be ignored but it's useful as the source for events.
	 * 
	 * @param clientToolkit Tools to help the handler do it's job.
	 * 
	 * @return An Handler object. Never null.
	 */
	public T createClientHandler(T proxy, ClientSideToolkit toolkit);

	
}
