package org.oddjob.events;

import org.oddjob.util.Restore;

import java.util.function.Consumer;

/**
 * Base class for sources of events.
 *
 * @param <T>
 */
abstract public class InstantEventSourceBase<T> extends EventSourceBase<InstantEvent<T>>
implements InstantEventSource<T> {

	@Override
	protected abstract Restore doStart(Consumer<? super InstantEvent<T>> consumer) throws Exception;


}
