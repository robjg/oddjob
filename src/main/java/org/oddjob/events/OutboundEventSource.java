package org.oddjob.events;

import org.oddjob.beanbus.Outbound;
import org.oddjob.util.Restore;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Create an {@link Outbound} consumer of a Bean Bus that is an {@link EventSource}.
 *
 * @param <T> The Pipeline Outbound type.
 */
public class OutboundEventSource<T> implements EventSource<T>, Consumer<T>, Outbound<T> {

    private final List<Consumer<? super T>> subscribers = new CopyOnWriteArrayList<>();

    private volatile Consumer<? super T> next;

    private volatile String name;

    @Override
    public void accept(T t) {
        for (Consumer<? super T> subscriber : subscribers) {
            subscriber.accept(t);
        }
        Optional.ofNullable(next).ifPresent(to -> to.accept(t));
    }

    @Override
    public Restore subscribe(Consumer<? super T> consumer) {
        subscribers.add(consumer);
        return () -> subscribers.remove(consumer);
    }

    public int getSubscriberCount() {
        return subscribers.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setTo(Consumer<? super T> destination) {
        this.next = destination;
    }

    public Consumer<? super T> getTo() {
        return next;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(this.name).orElseGet(() -> getClass().getSimpleName());
    }
}
