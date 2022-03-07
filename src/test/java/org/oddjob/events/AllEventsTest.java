package org.oddjob.events;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.oddjob.util.Restore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AllEventsTest {

    private static class IntEvents implements InstantEventSource<Integer> {

        Consumer<? super InstantEvent<Integer>> consumer;

        void publish(Integer i) {
            consumer.accept(InstantEvent.of(i));
        }

        @Override
        public Restore subscribe(Consumer<? super InstantEvent<Integer>> consumer) {
            this.consumer = consumer;
            return () -> {
            };
        }
    }

    private static class DoubleEvents implements InstantEventSource<Double> {

        Consumer<? super InstantEvent<Double>> consumer;

        void publish(Double d) {
            consumer.accept(InstantEvent.of(d));
        }

        @Override
        public Restore subscribe(Consumer<? super InstantEvent<Double>> consumer) {
            this.consumer = consumer;
            return () -> {
            };
        }
    }

    @Test
    public void givenTwoSourcesWhenOnlyBothPublishThenEventFired()
            throws Exception {

        IntEvents ie = new IntEvents();
        DoubleEvents de = new DoubleEvents();

        List<CompositeEvent<Number>> results = new ArrayList<>();

        AllEvents<Number> test = new AllEvents<>();

        List<EventSource<? extends InstantEvent<? extends Number>>> sources =
                Arrays.asList(ie, de);


        test.start(sources,
                results::add);

        assertThat(results.size(), is(0));

        ie.publish(1);

        assertThat(results.size(), is(0));

        de.publish(4.2);

        assertThat(results.size(), is(1));
        assertThat(EventConversions.toList(results.get(0)),
                is(Arrays.asList(1, 4.2)));

        de.publish(2.6);

        assertThat(results.size(), is(2));
        assertThat(EventConversions.toList(results.get(1)),
                is(Arrays.asList(1, 2.6)));
    }

    private static class TwoInitialEvents implements InstantEventSource<Integer> {

        @Override
        public Restore subscribe(Consumer<? super InstantEvent<Integer>> consumer) {
            consumer.accept(new WrapperOf<>(1, Instant.now()));
            consumer.accept(new WrapperOf<>(2, Instant.now()));
            return () -> {
            };
        }
    }

    @Test
    public void givenTwoSourcesWhenTwoInitialEventsThenFiresOnlyTwice() throws Exception {

        List<CompositeEvent<Integer>> results = new ArrayList<>();

        AllEvents<Integer> test = new AllEvents<>();

        List<InstantEventSource<Integer>> eventSources = Arrays.asList(new TwoInitialEvents(),
                new TwoInitialEvents());

        test.start(eventSources,
                results::add);

        assertThat("Results should be 3 but were: " + results,
                results.size(), is(3));
        assertThat(EventConversions.toList(results.get(0)),
                is(Arrays.asList(1, 1)));
        assertThat(EventConversions.toList(results.get(1)),
                is(Arrays.asList(2, 1)));
        assertThat(EventConversions.toList(results.get(2)),
                is(Arrays.asList(2, 2)));
    }

    public void testSimpleEvents() throws Exception {

        EventSource<String> es1 = consumer ->  {
            consumer.accept("a");
            return () -> {};
        };

        EventSource<String> es2 = consumer ->  {
            consumer.accept("b");
            return () -> {};
        };

        List<CompositeEvent<String>> results = new ArrayList<>();

        AllEvents<String> test = new AllEvents<>();

        AutoCloseable closeable = test.start(Arrays.asList(es1, es2), results::add);

        closeable.close();

        assertThat("Results should be 1 but were: " + results,
                results.size(), is(1));
        assertThat(EventConversions.toList(results.get(0)), CoreMatchers.hasItems( "a", "b"));
    }
}
