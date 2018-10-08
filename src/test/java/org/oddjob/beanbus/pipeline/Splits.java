package org.oddjob.beanbus.pipeline;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Splits {

    public static <T, U> Section<T, U> byName(Function<T, Collection<String>> mapping) {

        return new Section<T, U>() {

            Map<String, Consumer<? super U>> splits = new HashMap<>();

            @Override
            public Pipe<T> linkTo(Consumer<? super U> next) {

                Pipe<T> p = new Pipe<T>() {

                    @Override
                    public void accept(T data) {

                        Collection<String > sendTo = mapping.apply(data);
                        sendTo.forEach(name -> Optional.of( splits.get(name) ) );
                    }

                    @Override
                    public void flush() {

                    }
                };

                splits.put(next.toString(), next);

                return p;
            }
        };
    }

    public static <T> Section<T, T> byIndex(Function<? super T, ? extends Collection<Integer>> mapping) {

        return new Section<T, T>() {

            final List<Consumer<? super T>> splits = new ArrayList<>();

            final Pipe<T> pipe = new Pipe<T>() {

                @Override
                public void accept(T data) {

                    Collection<Integer> sendTo = mapping.apply(data);
                    sendTo.forEach(index-> Optional.of( splits.get(index))
                            .ifPresent(c -> c.accept(data)) );
                }

                @Override
                public void flush() {
                }
            };


            @Override
            public Pipe<T> linkTo(Consumer<? super T> next) {

                splits.add(next);

                return pipe;
            }
        };
    }

    public static <T> Section<T, T> toAll() {

        return new Section<T, T>() {

            List<Consumer<? super T>> splits = new ArrayList<>();

            @Override
            public Pipe<T> linkTo(Consumer<? super T> next) {

                Pipe<T> p = new Pipe<T>() {

                    @Override
                    public void accept(T data) {

                        splits.forEach( s -> s.accept(data) );
                    }

                    @Override
                    public void flush() {
                    }
                };

                splits.add(next);

                return p;
            }
        };
    }
}
