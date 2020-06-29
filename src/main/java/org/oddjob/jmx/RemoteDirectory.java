package org.oddjob.jmx;

import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ServerId;

/**
 * Extends the idea of a {@link BeanDirectory} to also includes a server
 * identifier.
 * 
 * @author rob
 *
 */
public interface RemoteDirectory extends BeanDirectory {

	/**
	 * Get the serverId.
	 * 
	 * @return The serverId.
	 */
	ServerId getServerId();
	
}
