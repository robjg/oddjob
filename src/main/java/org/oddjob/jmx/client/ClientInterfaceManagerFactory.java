/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

/**
 * Client side class for creating {@link ClientInterfaceManager}s.
 *
 * @author Rob Gordon
 */
public interface ClientInterfaceManagerFactory {

	Class<?>[] filter(Class<?>[] remoteSupports);

	ClientInterfaceManager create(
			Object source, 
			ClientSideToolkit csToolkit);
}