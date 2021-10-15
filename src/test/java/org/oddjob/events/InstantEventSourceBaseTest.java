package org.oddjob.events;

import org.junit.Test;
import org.oddjob.events.state.EventState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InstantEventSourceBaseTest {

    private static class OurSubscriber extends InstantEventSourceBase<String> {

        private boolean closed;

        private InstantEvent<String> initial;

        private Consumer<? super InstantEvent<String>> consumer;

        @Override
        protected Restore doStart(Consumer<? super InstantEvent<String>> consumer) {

            Optional.ofNullable(initial).ifPresent(consumer);
            this.consumer = consumer;
            return () -> closed = true;
        }
    }

    @Test
    public void testHappyPathStatesAsExpectedEventThere() throws Exception {

        OurSubscriber test = new OurSubscriber();
        test.initial = InstantEvent.of("Apple");

        List<InstantEvent<String>> results = new ArrayList<>();

        StateSteps subscriberState = new StateSteps(test);
        subscriberState.startCheck(EventState.READY, EventState.CONNECTING,
                EventState.FIRING, EventState.TRIGGERED);

        Restore restore = test.subscribe(results::add);

        subscriberState.checkNow();
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getOf(), is("Apple"));

        restore.close();

        assertThat(test.closed, is(true));
    }

    @Test
    public void testHappyPathStatesAsExpectedNoEventsYet() throws Exception {

        OurSubscriber test = new OurSubscriber();

        List<InstantEvent<String>> results = new ArrayList<>();

        StateSteps subscriberState = new StateSteps(test);
        subscriberState.startCheck(EventState.READY, EventState.CONNECTING,
                EventState.WAITING);

        Restore restore = test.subscribe(results::add);

        subscriberState.checkNow();

        assertThat(results.size(), is(0));

        subscriberState.startCheck(EventState.WAITING, EventState.FIRING, EventState.TRIGGERED);

        test.consumer.accept(InstantEvent.of("Apple"));

        subscriberState.checkNow();

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getOf(), is("Apple"));

        restore.close();

        assertThat(test.closed, is(true));
    }
}