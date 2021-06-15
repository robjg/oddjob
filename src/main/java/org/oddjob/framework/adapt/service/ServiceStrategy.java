package org.oddjob.framework.adapt.service;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.framework.adapt.AdaptorFactory;

import java.util.Optional;

/**
 * Something that can attempt to adapt a component to a service.
 * 
 * @author rob
 *
 */
public interface ServiceStrategy extends AdaptorFactory<ServiceAdaptor> {

	@Override
	default Optional<ServiceAdaptor> adapt(Object component, ArooaSession session) {
		return Optional.ofNullable(serviceFor(component, session));
	}

	/**
	 * Attempt to provide an adaptor.
	 * 
	 * @param component The component.
	 * @param session A session that might be useful.
	 * 
	 * @return The adaptor or null if this strategy can not provide the
	 * adaptor.
	 */
	ServiceAdaptor serviceFor(Object component, ArooaSession session);
}
