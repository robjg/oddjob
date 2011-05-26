package org.oddjob.jmx.client;

import java.io.Serializable;

import org.oddjob.arooa.ClassResolver;

/**
 * Able to create a {@link ClientInterfaceHandlerFactory}.
 * Instances of this will be sent accross the wire to allow
 * the client to access the factory needed to create the
 * client handler.
 * 
 * @author rob
 *
 * @param <T>
 */
public interface ClientHandlerResolver<T> extends Serializable {

	/**
	 * Provide the factory.
	 * 
	 * @param classResolver Allows resolver to find the class.
	 * 
	 * @return
	 */
	public ClientInterfaceHandlerFactory<T> resolve(
			ClassResolver classResolver);	
}
