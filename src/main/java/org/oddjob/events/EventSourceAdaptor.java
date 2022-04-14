package org.oddjob.events;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.beanbus.Outbound;
import org.oddjob.beanbus.adapt.OutboundStrategies;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provide an adapter to an {@link EventSource} from components that may be Event Sources already, or
 * may be {@link org.oddjob.beanbus.Outbound}s.
 *
 * @param <T> The type of event.
 */
public class EventSourceAdaptor<T> implements EventSource<T> {

    private static final Logger logger = LoggerFactory.getLogger(EventSourceAdaptor.class);

    private final Object original;

    private final Outbound<T> outbound;

    public EventSourceAdaptor(Object original, Outbound<T> outbound) {
        this.original = original;
        this.outbound = Objects.requireNonNull(outbound);
    }

    @Override
    public Restore subscribe(Consumer<? super T> consumer) {
        this.outbound.setTo(consumer);
        if (original instanceof Runnable) {
            ((Runnable)  original).run();
        }
        return () -> {
            if (original instanceof Stoppable) {
                try {
                    ((Stoppable) original).stop();
                } catch (FailedToStopException e) {
                    logger.error("Failed stopping {}:", original, e);
                }
            }
        };
    }

    @Override
    public String toString() {
        return "EventSourceAdaptor for [" +
                outbound +
                ']';
    }

    public static <T> Optional<EventSource<T>> maybeEventSourceFrom(Object maybe, ArooaSession session) {

        if (maybe instanceof EventSource) {
            //noinspection unchecked
            return Optional.of((EventSource<T>) maybe);
        }

        return OutboundStrategies.<T>maybeOutbound(maybe, session)
                .map(outbound1 -> new EventSourceAdaptor<T>(maybe, outbound1));
    }
}
