package org.oddjob.beanbus.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A collection of useful components to use in a pipeline.
 */
public class Pipes {

    private Pipes() {}

    /**
     * Create a filter section that will apply the given predicate to only allow data that tests true to continue
     * down the pipeline.
     *
     * @param test The predicate to apply to the data.
     *
     * @param <T> The type of the data.
     *
     * @return The filtering section.
     */
    public static <T> Section<T, T> test(Predicate<T> test) {
        return next -> data -> {
            if (test.test(data)) {
                next.accept(data);
            }
        };
    }

    /**
     * Map data in a pipeline using the given function.
     *
     * @param mapper The mapping function.
     *
     * @param <T> The type of incoming data.
     * @param <U> The type of outgoing data.
     *
     * @return The mapping section.
     */
    public static <T, U> Section<T, U> map(Function<? super T, ? extends  U> mapper) {
        return next -> data -> next.accept(mapper.apply(data));
    }

    /**
     * Create an identity section that just passes data along.
     *
     * @param <T> The type of the data.
     *
     * @return An identity section.
     */
    public static <T> Section<T, T> identity() {
        return map(Function.identity());
    }

    /**
     * Create a section that batches data into a list of the given size. Once a batch is assembled it passed to
     * the next component in the pipeline. On flush then any remaining items are sent to the next component
     * in the pipeline.
     *
     * @param batchSize The size of the batch.
     *
     * @param <T> The type of data being batched.
     *
     * @return A batching section.
     */
    public static <T> Section<T, List<T>> batcher(int batchSize) {
        return next -> new Pipe<T>() {

            private final AtomicInteger counter = new AtomicInteger();

            private final Queue<T> queue = new ConcurrentLinkedQueue<>();

            @Override
            public void accept(T data) {
                queue.add(data);
                if (counter.incrementAndGet() % batchSize == 0) {
                    List<T> batch = new ArrayList<>();
                    for (int i = 0; i < batchSize; ++i) {
                        batch.add(queue.remove());
                    }
                    next.accept(batch);
                }
            }

            @Override
            public void flush() {
                List<T> remaining = new ArrayList<>(queue);
                if (!remaining.isEmpty()) {
                    next.accept(remaining);
                }
            }
        };
    }

    /**
     * Apply a fold to the elements coming down the pipe. The result of the fold will be sent to the next
     * component in the pipe on flush.
     *
     * @param initialValue The initial value of the fold.
     * @param foldFunction A bi-function that takes the last (or initial value) and the current value and
     *                     and returns a new value as the ongoing folded value.
     *
     * @param <T> The type of incoming data.
     * @param <U> The type of the result of the fold that is the outgoing type.
     *
     * @return A fold section.
     */
    public static <T, U> Section<T, U> fold(U initialValue,
                                            BiFunction<? super U, ? super T, ? extends U> foldFunction) {

        return next -> new Pipe<T>() {

            private final AtomicReference<U> accumulator = new AtomicReference<>(initialValue);

            @Override
            public void accept(T t) {
                accumulator.accumulateAndGet(null, (a, ignore) -> foldFunction.apply(a, t));
            }

            @Override
            public void flush() {
                if (accumulator.get() != null) next.accept(accumulator.get());
            }

        };
    }

    /**
     * Count elements in a pipeline. The count is sent onwards on flush.
     *
     * @return A counting section.
     */
    public static Section<Object, Long> count() {

        return fold(0L, (a, x) -> ++a);
    }
}
