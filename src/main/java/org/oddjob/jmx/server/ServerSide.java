package org.oddjob.jmx.server;

/**
 * Encapsulate server side management.
 */
public interface ServerSide extends JmxServer, AutoCloseable {

    @Override
    void close();
}
