/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import org.oddjob.jmx.RemoteIdMappings;

import java.io.Serializable;

/**
 * A Transportable object is one which can be sent across the network and
 * resolved locally. Transportable objects typically contain addresses which
 * need to be resolved locally to identify a job relative to the server.
 *
 * @author Rob Gordon.
 */
public interface Transportable extends Serializable {
	
	/**
	 * Resolve this object into a local object.
	 * 
	 * @param names
	 * 
	 * @return An object which is valid in the local environment.
	 */
	Object importResolve(RemoteIdMappings names);
}
