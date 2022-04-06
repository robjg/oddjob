package org.oddjob.events;

import org.oddjob.util.Restore;

import java.util.function.Consumer;

/**
 * A source fo events.
 *
 * @param <T> The type of the event or thing that is the trigger for an event.
 */
public interface InstantEventSource<T> extends EventSource<InstantEvent<T>> {

	/**
	 * Start listening for events.
	 *
	 * @param consumer The consumer to receive events on.
	 *
	 * @return Something that will stop events being sent and free any resources that were used to send the events.
	 */
	Restore subscribe(Consumer<? super InstantEvent<T>> consumer);
}
