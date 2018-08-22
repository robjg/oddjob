package org.oddjob.persist;

import java.io.Serializable;

import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.life.ComponentPersister;

public class RestoreBundle implements Serializable {
	private static final long serialVersionUID = 2010032600L;

	private final ArooaConfiguration configuration;
	
	private final ComponentPersister archivePersister;
	
	public RestoreBundle(ArooaConfiguration configuration, 
			ComponentPersister archivePersister) {
		
		this.archivePersister = archivePersister;
		this.configuration = configuration;
	}

	public ArooaConfiguration getConfiguration() {
		return configuration;
	}

	public ComponentPersister getArchivePersister() {
		return archivePersister;
	}
	
}
