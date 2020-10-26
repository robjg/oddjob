/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import org.oddjob.arooa.ClassResolver;
import org.oddjob.remote.Implementation;

/**
 * Client side class for creating {@link ClientInterfaceManager}s.
 *
 * @author Rob Gordon
 */
public interface ClientInterfaceManagerFactory {


	interface Prepared {

		Class<?>[] supportedInterfaces();

		ClientInterfaceManager create(
				Object source,
				ClientSideToolkit csToolkit);
	}

	Prepared prepare(Implementation<?>[] remoteSupports, ClassResolver classResolver);

}