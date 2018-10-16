package org.oddjob.beanbus.pipeline;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Things that capture data. These will send data to the next section only once in the pipeline and only when
 * they are flushed. These will typically be the last component in a pipeline to capture results.
 */
public class Captures {

    private Captures() {}

    private static abstract class ResultsBase<T, X extends Collection<T>>
    implements Pipe<T> {

        private final Consumer<? super X> next;

        private final Queue<T> results = new ConcurrentLinkedQueue<>();

        ResultsBase(Consumer<? super X> next) {
            this.next = next;
        }

        abstract protected X collection();

        @Override
        public void accept(T t) {
            results.add(t);
        }

        @Override
        public void flush() {
            X copy = collection();
            for (T t = results.poll(); t != null; t = results.poll()) {
                copy.add(t);
            }
            next.accept(copy);
        }
    }

    /**
     * Create a section that consumes all data into a list of that data.
     *
     * @param <T> The type of the data.
     *
     * @return The section.
     */
    public static <T> Section<T, List<T>> toList() {

        return next -> new ResultsBase<T, List<T>>(next) {

            @Override
            protected List<T> collection() {
                return new ArrayList<>();
            }
        };
    }

    /**
     * Create a section that consumes all data into a set of that data.
     *
     * @param <T> The type of the data.
     *
     * @return The section.
     */
    public static <T> Section<T, Set<T>> toSet() {

        return next -> new ResultsBase<T, Set<T>>(next) {

            @Override
            protected Set<T> collection() {
                return new HashSet<>();
            }
        };
    }

}
