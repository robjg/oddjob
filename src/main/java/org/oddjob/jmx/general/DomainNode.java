package org.oddjob.jmx.general;

import org.oddjob.Structural;
import org.oddjob.jmx.client.Destroyable;

/**
 * Marker interface for an Object that represents a JMX domain.
 * 
 * @author rob
 *
 */
public interface DomainNode extends Structural, Destroyable {

	/**
	 * Called from the service to initialise the node.
	 */
	public void initialise();
	
}
