package org.oddjob.jmx;

import org.oddjob.arooa.registry.BeanDirectoryOwner;

/**
 * Something that is able to provide a {@link RemoteDirectory}
 * 
 * @author rob
 *
 */
public interface RemoteDirectoryOwner extends BeanDirectoryOwner {

	/**
	 * Provide a remote directory.
	 */
	public RemoteDirectory provideBeanDirectory();
}
