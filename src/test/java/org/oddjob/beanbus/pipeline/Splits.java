package org.oddjob.beanbus.pipeline;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Provides strategies for splitting data.
 */
public class Splits {

    private Splits() {}

    /**
     * Split by the name of the section being joined to. The name of the section is that given with the
     * {@link Pipeline.Options#named(String)} option.
     *
     * @param mapping The function mapping data to the name of the section.
     *
     * @param <T> The type of data being split.
     *
     * @return A Section that will do the splitting.
     */
    public static <T> Section<T, T> byName(Function<T, Collection<String>> mapping) {

        return new Splitter<T, T>() {

            Map<String, Consumer<? super T>> splits = new HashMap<>();

            @Override
            public Pipe<T> linkTo(Consumer<? super T> next) {

                Pipe<T> p = new Pipe<T>() {

                    @Override
                    public void accept(T data) {

                        Collection<String > sendTo = mapping.apply(data);
                        sendTo.forEach(name -> Optional.ofNullable(
                                splits.get(name) ).ifPresent(c -> c.accept(data)) );
                    }
                };

                splits.put(next.toString(), next);

                return p;
            }
        };
    }

    /**
     * Split by the index of the section being joined to.
     *
     * @param mapping The function mapping data to the name of the section.
     *
     * @param <T> The type of data being split.
     *
     * @return A Section that will do the splitting.
     */
    public static <T> Section<T, T> byIndex(BiFunction<? super T, Integer, ? extends Collection<Integer>> mapping) {

        return new Splitter<T, T>() {

            final List<Consumer<? super T>> splits = new ArrayList<>();

            final Pipe<T> pipe = new Pipe<T>() {

                @Override
                public void accept(T data) {

                    Collection<Integer> sendTo = mapping.apply(data, splits.size());
                    sendTo.forEach(index-> splits.get(index).accept(data));
                }
            };


            @Override
            public Pipe<T> linkTo(Consumer<? super T> next) {

                splits.add(next);

                return pipe;
            }
        };
    }

    /**
     * Send data to each consumer one after another.
     *
     * @param <T> The type of data.
     *
     * @return A new splitting section.
     */
    public static <T> Section<T, T> roundRobin() {

        AtomicInteger count = new AtomicInteger();

        return byIndex((ignored, size) ->
                size > 0 ? Collections.singleton(count.getAndIncrement() % size)
                        : Collections.emptyList());
    }
}
