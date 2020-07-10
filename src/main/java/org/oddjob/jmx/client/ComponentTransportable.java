/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import org.oddjob.framework.Transportable;
import org.oddjob.jmx.RemoteIdMappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * This object represents a component as it travels across the network between
 * client and server.
 *
 * @author Rob Gordon.
 */
public class ComponentTransportable implements Transportable {
	private static final long serialVersionUID=2020062900L;
	
	private static final Logger logger = LoggerFactory.getLogger(ComponentTransportable.class);
	
	/** The address which identify this component. */
	private final long remoteId;
	
	public ComponentTransportable(long remoteId) {
		this.remoteId = remoteId;
	}
		
	public Object importResolve(RemoteIdMappings names) {
		Object resolved = names.objectFor(remoteId);
		logger.debug("Resolved [" + resolved + "] from addresses [" + remoteId + "]");
		return resolved;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ComponentTransportable that = (ComponentTransportable) o;
		return remoteId == that.remoteId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(remoteId);
	}

	public String toString() {
		return "ComponentTransportable: " + remoteId;
	}
}
