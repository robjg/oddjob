package org.oddjob.events;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.oddjob.util.Restore;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EventOperatorBaseTest {

    static class OurSource implements InstantEventSource<String> {

        private final List<String> initial;

        private Consumer<? super InstantEvent<String>> consumer;

        OurSource(String... initial) {
            this.initial = Arrays.asList(initial);
        }

        @Override
        public Restore subscribe(Consumer<? super InstantEvent<String>> consumer) {
            initial.forEach(s -> consumer.accept(InstantEvent.of(s)));
            this.consumer = consumer;
            return () -> {
            };
        }

        void send(String s) {
            consumer.accept(InstantEvent.of(s));
        }
    }


    @Test
    public void testTriggerEventArrivesLater() throws Exception {

        Predicate<EventsArray<?>> predicate =
                array -> {
                    List<?> list =
                            array.toStream()
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .map( e -> ((InstantEvent<?>) e).getOf())
                                    .collect(Collectors.toList());

                    return list.contains("red") && list.contains("blue");
                };

        EventOperatorBase<String> test = new EventOperatorBase<>(predicate);

        List<CompositeEvent<String>> results = new ArrayList<>();

        OurSource source = new OurSource();
        List<InstantEventSource<String>> sources = Arrays.asList(
                new OurSource("blue"),
                source,
                new OurSource("green", "white")
        );

        test.start(sources, results::add);

        assertThat(results.size(), is(0));

        source.send("yellow");

        assertThat(results.size(), is(0));

        source.send("red");

        assertThat(results.size(), is(1));
    }

    @Test
    public void testTriggerArrivesBeforeSwitch() throws Exception {

        Predicate<EventsArray<?>> predicate =
                array -> {
                    List<?> list =
                            array.toStream()
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .map( e -> ((InstantEvent<?>) e).getOf())
                                    .collect(Collectors.toList());
                    return list.contains("red") && list.contains("blue");
                };

        EventOperatorBase<String> test = new EventOperatorBase<>(predicate);

        List<CompositeEvent<String>> results = new ArrayList<>();

        List<InstantEventSource<String>> sources = Arrays.asList(
                new OurSource("yellow", "blue"),
                new OurSource("red")
        );

        test.start(sources, results::add);

        assertThat(results.size(), is(1));
    }

    @Test
    public void testManyTriggers() throws Exception {

        EventOperatorBase<String> test = new EventOperatorBase<>(ignored -> true);

        List<CompositeEvent<String>> results = new ArrayList<>();

        OurSource source1 = new OurSource("yellow", "blue");
        OurSource source2 = new OurSource("green", "pink");

        List<InstantEventSource<String>> sources = Arrays.asList(source1, source2);

        test.start(sources, results::add);

        assertThat(results.size(), is(3));
        assertThat(EventConversions.toList(results.get(0)),
                is(Arrays.asList("yellow", "green")));
        assertThat(EventConversions.toList(results.get(1)),
                is(Arrays.asList("blue", "green")));
        assertThat(EventConversions.toList(results.get(2)),
                is(Arrays.asList("blue", "pink")));
    }

    @Test
    public void testManyThreads() throws Exception {

        final String[] many = new String[500];
        Arrays.fill(many, "");

        Thread[] threads = new Thread[10];
        OurSource[] sources = new OurSource[threads.length];

        for (int i = 0; i < threads.length; ++i) {
            sources[i] = new OurSource(many);
        }

        EventOperatorBase<String> test = new EventOperatorBase<>(ignored -> true);

        Queue<CompositeEvent<String>> results = new ConcurrentLinkedQueue<>();

        test.start(Arrays.asList(sources), results::add);

        assertThat(results.size(), is(threads.length * many.length - 9));

        for (int i = 0; i < threads.length; ++i) {
            final int fi = i;
            threads[i] = new Thread(() -> {
                for (String s : many) {
                    sources[fi].send(s);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertThat(results.size(), is(threads.length * many.length * 2 - 9));
    }

    @Test
    public void testGenericHeadache() {

        EventOperatorBase.EventsArrayImpl test = new EventOperatorBase.EventsArrayImpl(3);

        test.set(1, InstantEvent.of(1));

        List<Object> results = new ArrayList<>();

        for (Optional<?> e : test) {

            results.add(e.map(v -> ((InstantEvent<?>) v).getOf()).orElse(null));
        }

        assertThat(results.get(0), CoreMatchers.nullValue());
        assertThat(results.get(1), CoreMatchers.is(1));
        assertThat(results.get(2), CoreMatchers.nullValue());
    }

}