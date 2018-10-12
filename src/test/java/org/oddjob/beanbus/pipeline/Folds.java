package org.oddjob.beanbus.pipeline;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Folds {

    private Folds() {}

    public static <T, U> Section<T, U> with(U initialValue, BiFunction<? super U, ? super T, ? extends U> f) {

        return new Fold<>(initialValue, f );
    }

    public static  Section<Integer, Integer> maxInt() {

        return new Fold<>(Integer.MIN_VALUE, (a, x) -> Math.max(a, x));
    }

    public static  Section<Long, Long> maxLong() {

        return new Fold<>(Long.MIN_VALUE, (a, x) -> Math.max(a, x));
    }

    public static  Section<Double, Double> maxDouble() {

        return new Fold<>(Double.MIN_VALUE, (a, x) -> Math.max(a, x));
    }

    public static Section<Object, Long> count() {

        return new Fold<>(0L, (a, x) -> ++a);
    }

    private static class Fold<T, U> implements Section<T, U> {

        private final U initialValue;

        private final BiFunction<? super U, ? super T, ? extends U> f;

        Fold(U initialValue, BiFunction<? super U, ? super T, ? extends U> f) {
            Objects.requireNonNull(initialValue);
            Objects.requireNonNull(f);
            this.initialValue = initialValue;
            this.f = f;
        }

        @Override
        public Pipe<T> linkTo(Consumer<? super U> next) {
            return new Pipe<T>() {

                private final AtomicReference<U> accumulator = new AtomicReference<>(initialValue);

                @Override
                public void accept(T t) {
                    accumulator.accumulateAndGet(null, (a, ignore) -> f.apply(a, t));
                }

                @Override
                public void flush() {
                    if (accumulator.get() != null) next.accept(accumulator.get());
                }

            };
        }
    }


}
