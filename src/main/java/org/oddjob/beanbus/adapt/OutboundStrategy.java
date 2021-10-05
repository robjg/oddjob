package org.oddjob.beanbus.adapt;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.beanbus.Outbound;

/**
 * Something that can attempt to adapt a component to an {@link Outbound}.
 * 
 * @author rob
 *
 */
public interface OutboundStrategy {

	/**
	 * Attempt to provide an {@link Outbound}.
	 * 
	 * @param component The component or its proxy.
	 * @param session A session that might be useful.
	 * 
	 * @return An Outbound or null if this strategy can not provide it.
	 */
	<T> Outbound<T> outboundFor(Object component, ArooaSession session);
}
