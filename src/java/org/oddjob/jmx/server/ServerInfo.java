/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import java.io.Serializable;

import org.oddjob.arooa.registry.Address;
import org.oddjob.jmx.client.ClientHandlerResolver;

/**
 * ServerInfo is a collection of information 
 * for the client about a server side bean. It's intention
 * is to provide a client with enough information to 
 * create a proxy for the server side component.
 * <p>
 * TODO: This is badly named. Should be ServerComponentInfo.
 *
 * @author Rob Gordon.
 */
public class ServerInfo implements Serializable {
	private static final long serialVersionUID = 2009090500L;
	
	private final Address address;
	
	/** The set of interfaces the server side
	 * component supports. */
	private final ClientHandlerResolver[] clientResolvers;
	
	/**
	 * Constructor.
	 * 
	 * @param id The id, can be null.
	 * @param url The url, must not be null.
	 * @param consoleId The console id, must not be null.
	 * @param resolvers Array of suported interfaces. Must not be null.
	 */
	public ServerInfo(
			Address address,
			ClientHandlerResolver[] resolvers) {
		if (resolvers == null) {
			throw new NullPointerException("interfaces must not be null.");
		}
		
		this.address = address;
		this.clientResolvers = resolvers;
	}

	/**
	 * Get the id of the component.
	 * 
	 * @return The id, may be null if the component doesn't
	 * have an id.
	 */
	public String getId() {
		return address.getPath().getId();
	}

	/**
	 * Get the factories that should be used to create handlers
	 * to talk to the server.
	 * <p>
	 * 
	 * @return The factories.
	 */
	public ClientHandlerResolver[] getClientResolvers() {
		return clientResolvers;
	}

	/**
	 * Get the address of this component. Only components with ids 
	 * will provide an address.
	 * 
	 * @return
	 */
	public Address getAddress() {
		return address;
	}
}
