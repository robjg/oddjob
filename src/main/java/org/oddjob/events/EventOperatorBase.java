package org.oddjob.events;

import org.oddjob.util.Restore;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Base class for event operators.
 *
 * @param <T> The type of the event.
 */
public class EventOperatorBase<T> implements EventOperator<T> {

    private final Predicate<List<T>> predicate;

    public EventOperatorBase(Predicate<List<T>> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Restore start(List<T> previous,
                         List<? extends EventSource<? extends T>> nodes,
                         Consumer<? super List<T>> results) throws Exception {

        int number = nodes.size();

        final List<Restore> closes = new ArrayList<>();

        Switch<T> theSwitch = new Switch<>(predicate, results, number);

        for (int i = 0; i < number; ++i) {
            closes.add(nodes.get(i)
                    .start(new InputConsumer<>(i, theSwitch)));
        }

        theSwitch.doSwitch();

        return () -> closes.forEach(Restore::close);
    }

    static class ValueByIndex<T> {

        private final T value;

        private final int index;

        ValueByIndex(T value, int index) {
            this.value = value;
            this.index = index;
        }
    }


    static class Switch<T> implements Consumer<ValueByIndex<T>> {

        private final Queue<ValueByIndex<T>> queue = new ConcurrentLinkedQueue<>();

        private final Predicate<List<T>> predicate;

        private volatile boolean switched;

        // create a chain of processing using completable future allow safe publication of our ongoing list
        // otherwise we'd have to synchronise it.
        private final AtomicReference<CompletableFuture<List<T>>> ref = new AtomicReference<>();

        private final Consumer<? super List<T>> resultConsumer;

        private final int size;

        Switch(Predicate<List<T>> predicate,
               Consumer<? super List<T>> resultConsumer, int size) {
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

        List<T> processQueue(List<T> sofar) {
            return processOutstanding(sofar, queue);
        }

        List<T> processOutstanding(List<T> sofar, Queue<ValueByIndex<T>> outStanding) {

            for (ValueByIndex<T> item = outStanding.poll(); item != null; item = outStanding.poll()) {
                sofar.set(item.index, item.value);
                test(sofar);
            }
            return sofar;
        }

        void test(List<T> events) {
            if (predicate.test(events)) {
                resultConsumer.accept(new ArrayList<>(events));
            }
        }

        void doSwitch() {

            List<T> initial = new ArrayList<>(size);
            for (int i = 0; i < size; ++i) {
                initial.add(null);
            }

            Queue<ValueByIndex<T>> outstanding = new LinkedList<>();

            for (ValueByIndex<T> item = queue.poll(); item != null; item = queue.poll()) {
                if (initial.get(item.index) == null) {
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

        private final Consumer<? super ValueByIndex<T>> results;

        private final int index;

        InputConsumer(int index,
                      Consumer<? super ValueByIndex<T>> results) {
            this.index = index;
            this.results = results;
        }

        @Override
        public void accept(T t) {
            results.accept(new ValueByIndex<>(t, index));
        }
    }

}
