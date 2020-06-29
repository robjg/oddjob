/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import org.oddjob.framework.Transportable;
import org.oddjob.jmx.ObjectNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final long name;
	
	public ComponentTransportable(long name) {
		this.name = name;
	}
		
	public Object importResolve(ObjectNames names) {
		Object resolved = names.objectFor(name);
		logger.debug("Resolved [" + resolved + "] from addresses [" + name + "]");
		return resolved;
	}
	
	public String toString() {
		return "ComponentTransportable: " + name;
	}
}
