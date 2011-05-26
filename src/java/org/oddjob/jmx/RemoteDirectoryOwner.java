package org.oddjob.jmx;

import org.oddjob.arooa.registry.BeanDirectoryOwner;

public interface RemoteDirectoryOwner extends BeanDirectoryOwner {

	public RemoteDirectory provideBeanDirectory();
}
