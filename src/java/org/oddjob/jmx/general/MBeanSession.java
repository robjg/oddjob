package org.oddjob.jmx.general;

import org.oddjob.arooa.ArooaSession;

/**
 * Provide useful things for classes implements the JMXService.
 * 
 * @author rob
 *
 */
public interface MBeanSession {

	/**
	 * Get the current Oddjob {@link ArooaSession}.
	 * 
	 * @return The session. Never null.
	 */
	public ArooaSession getArooaSession();
	
	/**
	 * Get the current {@link MBeanCache}.
	 * 
	 * @return The cache. Never null.
	 */
	public MBeanCache getMBeanCache();
}
