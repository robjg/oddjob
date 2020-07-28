package org.oddjob.jmx.server;

import org.oddjob.jmx.RemoteIdMappings;

/**
 * Encapsulate server side management.
 */
public interface ServerSide extends AutoCloseable {

    RemoteIdMappings getRemoteIdMappings();

    @Override
    void close();
}
