package org.oddjob.beanbus.pipeline;

import java.util.function.Function;
import java.util.function.Predicate;

public class Pipes {

    private Pipes() {}

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
     * @param <T> The type of incoming data.
     * @param <U> The type of outgoing data.
     */
    public static <T, U> Section<T, U> map(Function<? super T, ? extends  U> mapper) {
        return next -> data -> next.accept(mapper.apply(data));
    }

    public static <X> Section<X, X> identity() {
        return map(Function.identity());
    }
}
