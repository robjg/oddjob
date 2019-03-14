package org.oddjob.events;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Conversions for events.
 */
public class EventConversions {

    public static <T> List<T> toList(CompositeEvent<T> compositeEvent) {
        return compositeEvent
                .stream()
                .flatMap(EventConversions::flatten)
                .collect(Collectors.toList());
    }

    static <T> Stream<T> flatten(EventOf<T> eventOf) {
        if (eventOf instanceof CompositeEvent) {
            return ((CompositeEvent<T>) eventOf)
                    .stream()
                    .flatMap(EventConversions::flatten);
        } else {
            return Stream.of(eventOf.getOf());
        }
    }
}
