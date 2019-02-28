package org.oddjob.events;

import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Conversions for events.
 */
public class EventConversions implements ConversionProvider {

    @Override
    public void registerWith(ConversionRegistry registry) {

        registry.register(CompositeEvent.class, List.class,
                          EventConversions::toList);
    }

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
