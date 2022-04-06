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
import static org.hamcrest.MatcherAssert.assertThat;

public class EventWatchComponentTest {

    private static class OurEventSource implements EventSource<String> {

        private boolean closed;

        private String initial;

        private Consumer<? super String> consumer;

        @Override
        public Restore subscribe(Consumer<? super String> consumer) {

            Optional.ofNullable(initial).ifPresent(consumer);
            this.consumer = consumer;
            return () -> closed = true;
        }
    }

    @Test
    public void testHappyPathStatesAsExpectedEventThere() throws Exception {

        EventWatchComponent<String> test = new EventWatchComponent<>();

        OurEventSource eventSource = new OurEventSource();
        test.setEventSource(eventSource);

        eventSource.initial = "Apple";

        List<String> results = new ArrayList<>();
        test.setTo(results::add);

        StateSteps subscriberState = new StateSteps(test);
        subscriberState.startCheck(EventState.READY, EventState.CONNECTING,
                EventState.FIRING, EventState.TRIGGERED);

        test.run();

        subscriberState.checkNow();
        assertThat(results.size(), is(1));

        assertThat(results.get(0), is("Apple"));

        test.stop();

        assertThat(eventSource.closed, is(true));
    }

    @Test
    public void testHappyPathStatesAsExpectedNoEventsYet() throws Exception {

        EventWatchComponent<String> test = new EventWatchComponent<>();

        OurEventSource eventSource = new OurEventSource();
        test.setEventSource(eventSource);

        List<String> results = new ArrayList<>();
        test.setTo(results::add);

        StateSteps subscriberState = new StateSteps(test);
        subscriberState.startCheck(EventState.READY, EventState.CONNECTING,
                EventState.WAITING);

        test.run();

        subscriberState.checkNow();

        assertThat(results.size(), is(0));

        subscriberState.startCheck(EventState.WAITING, EventState.FIRING, EventState.TRIGGERED);

        eventSource.consumer.accept("Apple");

        subscriberState.checkNow();

        assertThat(results.size(), is(1));
        assertThat(results.get(0), is("Apple"));

        test.stop();

        assertThat(eventSource.closed, is(true));
    }
}