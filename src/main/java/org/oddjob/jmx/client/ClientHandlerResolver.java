package org.oddjob.jmx.client;

import org.oddjob.arooa.ClassResolver;

import java.io.Serializable;

/**
 * Able to create a {@link ClientInterfaceHandlerFactory}.
 * Instances of this will be sent across the wire to allow
 * the client to access the factory needed to create the
 * client handler.
 *
 * @param <T> The type of the {@link ClientInterfaceHandlerFactory}
 *            found.
 * @author rob
 */
public interface ClientHandlerResolver<T> extends Serializable {

    /**
     * Provide the factory.
     *
     * @param classResolver Allows resolver to find the class.
     * @return
     */
    ClientInterfaceHandlerFactory<T> resolve(
            ClassResolver classResolver);
}
