package org.oddjob.framework.adapt;

import org.oddjob.arooa.ArooaSession;

import java.util.Optional;

/**
 * Something that maybe can create an adapter to an interface that can be used
 * by a proxy to make the component implement the interface.
 *
 * @param <T> The type of the adaptor.
 */
public interface AdaptorFactory<T> {

    /**
     * Maybe provide a adaptor for the component.
     *
     * @param component The component that the adapter will be for.
     * @param session The session for descriptor and tools.
     *
     * @return Maybe an adapter.
     */
    Optional<T> adapt(Object component, ArooaSession session);

}
