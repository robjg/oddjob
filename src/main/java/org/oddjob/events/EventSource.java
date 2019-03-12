package org.oddjob.events;

import java.util.function.Consumer;

import org.oddjob.util.Restore;

/**
 * A source fo events.
 *
 * @param <T> The type of the event or thing that is the trigger for an event.
 */
public interface EventSource<T> {

	/**
	 * Start listening for events.
	 *
	 * @param consumer The consumer to receive events on.
	 *
	 * @return Something that will stop events being sent and free any resources that were used to send the events.
	 *
	 * @throws Exception If the listening could not be started.
	 */
	Restore start(Consumer<? super EventOf<T>> consumer) throws Exception;
}
