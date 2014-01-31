package org.oddjob.framework;

import org.oddjob.arooa.ArooaSession;

/**
 * Something that can attempt to adapt a component to a service.
 * 
 * @author rob
 *
 */
public interface ServiceStrategy {

	/**
	 * Attempt to provide an adaptor.
	 * 
	 * @param component The component.
	 * @param session A session that might be useful.
	 * 
	 * @return The adaptor or null if this strategy can not provide the
	 * adaptor.
	 */
	public ServiceAdaptor serviceFor(Object component, ArooaSession session);
}
