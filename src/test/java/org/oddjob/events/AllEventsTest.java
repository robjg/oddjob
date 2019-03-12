package org.oddjob.events;

import org.junit.Test;
import org.oddjob.util.Restore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AllEventsTest {

    private class IntEvents implements EventSource<Integer> {

        Consumer<? super EventOf<Integer>> consumer;

        void publish(Integer i) {
            consumer.accept(EventOf.of(i));
        }

        @Override
        public Restore start(Consumer<? super EventOf<Integer>> consumer) {
            this.consumer = consumer;
            return () -> {
            };
        }
    }

    private class DoubleEvents implements EventSource<Double> {

        Consumer<? super EventOf<Double>> consumer;

        void publish(Double d) {
            consumer.accept(EventOf.of(d));
        }

        @Override
        public Restore start(Consumer<? super EventOf<Double>> consumer) {
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

        List<EventSource<? extends Number>> sources =
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

    private class TwoInitialEvents implements EventSource<Integer> {

        @Override
        public Restore start(Consumer<? super EventOf<Integer>> consumer) {
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

        List<EventSource<Integer>> eventSources = Arrays.asList(new TwoInitialEvents(),
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

}
