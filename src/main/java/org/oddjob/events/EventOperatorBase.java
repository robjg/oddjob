package org.oddjob.events;

import org.oddjob.util.Restore;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for event operators.
 *
 * @param <T> The type of the event.
 */
public class EventOperatorBase<T> implements EventOperator<T> {

    private final Predicate<EventsArray<T>> predicate;

    private volatile EventOfFactory<T> eventOfFactory;

    public EventOperatorBase(Predicate<EventsArray<T>> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Restore start(List<? extends EventSource<? extends T>> nodes,
                         Consumer<? super CompositeEvent<T>> results)
            throws Exception {

        EventOfFactory<T> eventOfFactory =
                Optional.ofNullable(this.eventOfFactory)
                        .orElse(new WrapperOfFactory<T>().toValue());

        int number = nodes.size();

        final List<Restore> closes = new ArrayList<>();

        Switch<T> theSwitch = new Switch<>(predicate, results, number);

        for (int i = 0; i < number; ++i) {
            closes.add(nodes.get(i)
                            .start(new InputConsumer<>(
                                    eventOfFactory,
                                    i,
                                    theSwitch)));
        }

        theSwitch.doSwitch();

        return () -> closes.forEach(Restore::close);
    }

    public EventOfFactory<T> getEventOfFactory() {
        return eventOfFactory;
    }

    public void setEventOfFactory(EventOfFactory<T> eventOfFactory) {
        this.eventOfFactory = eventOfFactory;
    }

    static class ValueByIndex<T> {

        private final EventOf<T> value;

        private final int index;

        ValueByIndex(EventOf<T> value, int index) {
            this.value = value;
            this.index = index;
        }
    }


    static class Switch<T> implements Consumer<ValueByIndex<T>> {

        private final Queue<ValueByIndex<T>> queue = new ConcurrentLinkedQueue<>();

        private final Predicate<EventsArray<T>> predicate;

        private volatile boolean switched;

        // create a chain of processing using completable future allow safe publication of our ongoing list
        // otherwise we'd have to synchronise it.
        private final AtomicReference<CompletableFuture<EventsArrayImpl<T>>>
                ref = new AtomicReference<>();

        private final Consumer<? super CompositeEvent<T>> resultConsumer;

        private final int size;

        Switch(Predicate<EventsArray<T>> predicate,
               Consumer<? super CompositeEvent<T>> resultConsumer, int size) {
            this.predicate = predicate;
            this.resultConsumer = resultConsumer;
            this.size = size;
        }

        @Override
        public void accept(ValueByIndex<T> tValueByIndex) {

            queue.add(tValueByIndex);
            if (switched) {
                process();
            }
        }

        void process() {
            ref.updateAndGet(cf ->
                                     cf.thenApply(this::processQueue));
        }

        EventsArrayImpl<T> processQueue(EventsArrayImpl<T> sofar) {
            return processOutstanding(sofar, queue);
        }

        EventsArrayImpl<T> processOutstanding(EventsArrayImpl<T> sofar, Queue<ValueByIndex<T>> outStanding) {

            for (ValueByIndex<T> item = outStanding.poll(); item != null; item = outStanding.poll()) {
                sofar.set(item.index, item.value);
                test(sofar);
            }
            return sofar;
        }

        void test(EventsArrayImpl<T> events) {
            if (predicate.test(events)) {
                resultConsumer.accept(events.toCompositeEvent());
            }
        }

        void doSwitch() {

            EventsArrayImpl<T> initial = new EventsArrayImpl<>(size);

            Queue<ValueByIndex<T>> outstanding = new LinkedList<>();

            for (ValueByIndex<T> item = queue.poll(); item != null; item = queue.poll()) {
                if (!initial.getEventAt(item.index).isPresent()) {
                    initial.set(item.index, item.value);
                } else {
                    outstanding.add(item);
                }
            }

            test(initial);

            ref.set(CompletableFuture.completedFuture(processOutstanding(initial, outstanding)));

            switched = true;

            // It's possible that an event was received between polling the queue above and setting switched to true.
            process();
        }
    }

    static class InputConsumer<T> implements Consumer<T> {

        private final EventOfFactory<T> eventOfFactory;

        private final Consumer<? super ValueByIndex<T>> results;

        private final int index;

        InputConsumer(EventOfFactory<T> eventOfFactory,
                      int index,
                      Consumer<? super ValueByIndex<T>> results) {
            this.eventOfFactory = eventOfFactory;
            this.index = index;
            this.results = results;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void accept(T t) {
            if (t instanceof EventOf) {
                results.accept(new ValueByIndex<>((EventOf<T>) t,
                                                  index));
            } else {
                results.accept(new ValueByIndex<>(eventOfFactory.create(t),
                                                  index));
            }
        }
    }

    static class EventsArrayImpl<T> implements EventsArray<T> {

        private final List<Optional<EventOf<T>>> elements;

        EventsArrayImpl(int size) {

            elements = new ArrayList<>(size);
            for (int i = 0; i < size; ++i) {
                elements.add(Optional.empty());
            }
        }

        @Override
        public int getSize() {
            return elements.size();
        }

        @Override
        public Optional<EventOf<T>> getEventAt(int index) {
            return elements.get(index);
        }

        @Override
        public Stream<Optional<EventOf<T>>> toStream() {
            return elements.stream();
        }

        public void set(int index, EventOf<T> value) {
            elements.set(index, Optional.of(value));
        }

        public CompositeEvent<T> toCompositeEvent() {
            return new CompositeEventList<>(
                    elements.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList()));
        }
    }
}
