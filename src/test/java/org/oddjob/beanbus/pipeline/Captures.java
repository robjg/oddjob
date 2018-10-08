package org.oddjob.beanbus.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class Captures {

    private static abstract class ResultsBase<T, X extends Collection<T>>
    implements Pipe<T> {

        private final Consumer<? super X> next;

        private final Queue<T> results = new ConcurrentLinkedQueue<>();

        protected ResultsBase(Consumer<? super X> next) {
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

    public static <T> Section<T, List<T>> toList() {

        return next -> new ResultsBase<T, List<T>>(next) {

            @Override
            protected List<T> collection() {
                return new ArrayList<>();
            }
        };
    }

    public static <T> Section<T, T> single() {

        return next -> new Pipe<T>() {

            @Override
            public void accept(T t) {
                next.accept(t);
            }

            @Override
            public void flush() {

            }

        };
   }

}
