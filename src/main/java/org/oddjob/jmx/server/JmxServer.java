package org.oddjob.jmx.server;

import org.oddjob.jmx.RemoteIdMappings;

import javax.management.MBeanServerConnection;

/**
 * Abstraction used for the Web Handlers (in project oj-web) that use the JMX server for tracking
 * Oddjob changes.
 */
public interface JmxServer {

    RemoteIdMappings getRemoteIdMappings();

    MBeanServerConnection getServerConnection();
}
