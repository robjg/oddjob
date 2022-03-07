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

    private final Predicate<EventsArray<?>> predicate;

    public EventOperatorBase(Predicate<EventsArray<?>> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Restore start(List<? extends EventSource<?>> nodes,
                         Consumer<? super CompositeEvent<T>> results)
            throws Exception {

        int number = nodes.size();

        final List<Restore> closes = new ArrayList<>();

        Switch<T> theSwitch = new Switch<>(predicate, results, number);

        for (int i = 0; i < number; ++i) {
            closes.add(nodes.get(i)
                    .subscribe(new InputConsumer<>(
                            i,
                            theSwitch)));
        }

        theSwitch.doSwitch();

        return () -> closes.forEach(Restore::close);
    }

    static class EventByIndex {

        private final InstantEvent<Object> event;

        private final int index;

        @SuppressWarnings("unchecked")
        EventByIndex(Object event, int index) {
            InstantEvent<Object> instantEvent;
            if (event instanceof InstantEvent) {
                this.event = (InstantEvent<Object>) event;
            }
            else {
                this.event = InstantEvent.of(event);
            }
            this.index = index;
        }
    }


    static class Switch<T> implements Consumer<EventByIndex> {

        private final Queue<EventByIndex> queue = new ConcurrentLinkedQueue<>();

        private final Predicate<EventsArray<?>> predicate;

        private volatile boolean switched;

        // create a chain of processing using completable future allow safe publication of our ongoing list
        // otherwise we'd have to synchronise it.
        private final AtomicReference<CompletableFuture<EventsArrayImpl>>
                ref = new AtomicReference<>();

        private final Consumer<? super CompositeEvent<T>> resultConsumer;

        private final int size;

        Switch(Predicate<EventsArray<?>> predicate,
               Consumer<? super CompositeEvent<T>> resultConsumer, int size) {
            this.predicate = predicate;
            this.resultConsumer = resultConsumer;
            this.size = size;
        }

        @Override
        public void accept(EventByIndex tEventByIndex) {

            queue.add(tEventByIndex);
            if (switched) {
                process();
            }
        }

        void process() {
            ref.updateAndGet(cf ->
                    cf.thenApply(this::processQueue));
        }

        EventsArrayImpl processQueue(EventsArrayImpl sofar) {
            return processOutstanding(sofar, queue);
        }

        EventsArrayImpl processOutstanding(EventsArrayImpl sofar, Queue<EventByIndex> outStanding) {

            for (EventByIndex item = outStanding.poll(); item != null; item = outStanding.poll()) {
                sofar.set(item.index, item.event);
                test(sofar);
            }
            return sofar;
        }

        void test(EventsArrayImpl events) {
            if (predicate.test(events)) {
                resultConsumer.accept(events.toCompositeEvent());
            }
        }

        void doSwitch() {

            EventsArrayImpl initial = new EventsArrayImpl(size);

            Queue<EventByIndex> outstanding = new LinkedList<>();

            for (EventByIndex item = queue.poll(); item != null; item = queue.poll()) {
                if (!initial.getEventAt(item.index).isPresent()) {
                    initial.set(item.index, item.event);
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

    static class InputConsumer<T> implements Consumer<Object> {

        private final Consumer<? super EventByIndex> results;

        private final int index;

        InputConsumer(int index,
                      Consumer<? super EventByIndex> results) {
            this.index = index;
            this.results = results;
        }

        @Override
        public void accept(Object t) {
            results.accept(new EventByIndex(t, index));
        }
    }

    static class EventsArrayImpl implements EventsArray<Object> {

        private final List<Optional<InstantEvent<Object>>> elements;

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
        public Optional<InstantEvent<Object>> getEventAt(int index) {
            return elements.get(index);
        }

        @Override
        public Stream<Optional<InstantEvent<Object>>> toStream() {
            return elements.stream();
        }

        public void set(int index, InstantEvent<Object> instantEvent) {
            elements.set(index, Optional.of(instantEvent));
        }

        @SuppressWarnings("unchecked")
        public <T> CompositeEvent<T> toCompositeEvent() {

            List<InstantEvent<? extends T>> eventList = elements.stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(event -> (InstantEvent<T>) event)
                    .collect(Collectors.toList());

            return new CompositeEventList<>(eventList);
        }
    }
}
