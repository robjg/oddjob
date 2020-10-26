package org.oddjob.remote;

import java.io.Serializable;

/**
 * An {@link org.oddjob.jmx.server.ServerInterfaceHandler} implements this
 * if there is initialisation data to send with the initial
 * {@link org.oddjob.jmx.server.ServerInfo}. This can reduce chatter between
 * client and server.
 *
 * @param <T> The data type of the initialisation data.
 */
public interface HasInitialisation<T extends Serializable> {

    /**
     * Provide the initialisation.
     *
     * @return An Initialisation object. Should not be null.
     */
    Initialisation<T> initialisation();
}
