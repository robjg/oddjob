package org.oddjob.events;

import org.junit.Test;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.events.state.EventState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ForEventsTest {

    public static class SubscribeInts extends EventSourceBase<Integer> {

        @Override
        protected Restore doStart(Consumer<? super EventOf<Integer>> consumer) {
            IntStream.of(1, 2, 3).forEach(i -> consumer.accept(EventOf.of(i)));
            return () -> {
            };
        }
    }

    @Test
    public void givenHappyPathThenWorks() throws Exception {

        String xml = "<events id='x'>" +
                "<job>"
                + "<bean class='" + SubscribeInts.class.getName() + "'" +
                "  name='Subuscribe ${x.current}'/>" +
                "</job>" +
                "</events>";


        ForEvents<Integer> test = new ForEvents<>();
        test.setConfiguration(new XMLConfiguration("XML", xml));
        test.setValues(Stream.of(10, 20));
        test.setArooaSession(new StandardArooaSession());

        List<EventOf<Integer>> results = new ArrayList<>();

        StateSteps state = new StateSteps(test);
        state.startCheck(EventState.READY, EventState.CONNECTING,
                         EventState.FIRING, EventState.TRIGGERED,
                         EventState.FIRING, EventState.TRIGGERED,
                         EventState.FIRING, EventState.TRIGGERED,
                         EventState.FIRING, EventState.TRIGGERED,
                         EventState.FIRING, EventState.TRIGGERED);

        Restore close = test.start(results::add);

        state.checkNow();

        assertThat("Expected 5, was " + results,
                   results.size(), is(5));
        assertThat(EventConversions.toList((CompositeEvent<Integer>) results.get(0)),
                   is(Arrays.asList(1, 1)));
        assertThat(EventConversions.toList((CompositeEvent<Integer>) results.get(1)),
                   is(Arrays.asList(2, 1)));
        assertThat(EventConversions.toList((CompositeEvent<Integer>) results.get(2)),
                   is(Arrays.asList(3, 1)));
        assertThat(EventConversions.toList((CompositeEvent<Integer>) results.get(3)),
                   is(Arrays.asList(3, 2)));
        assertThat(EventConversions.toList((CompositeEvent<Integer>) results.get(4)),
                   is(Arrays.asList(3, 3)));

        state.startCheck(EventState.TRIGGERED, EventState.COMPLETE);

        close.close();

        state.checkNow();
    }

    private static Consumer<? super EventOf<String>> stuffConsumer;

    public static class SubscribeStuff extends EventSourceBase<String> {

        @Override
        protected Restore doStart(Consumer<? super EventOf<String>> consumer) {
            stuffConsumer = consumer;
            return () -> stuffConsumer = null;
        }

        void send(String stuff) {
            stuffConsumer.accept(EventOf.of(stuff));
        }
    }

    @Test
    public void whenTriggeredThenStops() throws Exception {

        String xml = "<events id='x'>" +
                "<job>"
                + "<bean class='" + SubscribeStuff.class.getName() + "'" +
                "  name='Subuscribe ${x.current}'/>" +
                "</job>" +
                "</events>";


        ForEvents<String> test = new ForEvents<>();
        test.setConfiguration(new XMLConfiguration("XML", xml));
        test.setValues(Stream.of("Foo"));
        test.setArooaSession(new StandardArooaSession());

        List<EventOf<String>> results = new ArrayList<>();

        StateSteps state = new StateSteps(test);
        state.startCheck(EventState.READY, EventState.CONNECTING,
                         EventState.WAITING);

        Restore close = test.start(results::add);

        state.checkNow();

        state.startCheck(EventState.WAITING, EventState.FIRING, EventState.TRIGGERED);

        stuffConsumer.accept(EventOf.of("Hello"));

        state.checkNow();

        assertThat(results.size(), is(1));
        assertThat(EventConversions.toList((CompositeEvent<String>) results.get(0)),
                   is(Arrays.asList("Hello")));

        state.startCheck(EventState.TRIGGERED, EventState.COMPLETE);

        close.close();

        state.checkNow();
    }

    @Test
    public void whenNotTriggeredThenStops() throws Exception {

        String xml = "<events id='x'>" +
                "<job>"
                + "<bean class='" + SubscribeStuff.class.getName() + "'" +
                "  name='Subuscribe ${x.current}'/>" +
                "</job>" +
                "</events>";


        ForEvents<String> test = new ForEvents<>();
        test.setConfiguration(new XMLConfiguration("XML", xml));
        test.setValues(Stream.of(10, 20));
        test.setArooaSession(new StandardArooaSession());

        List<EventOf<String>> results = new ArrayList<>();

        StateSteps state = new StateSteps(test);
        state.startCheck(EventState.READY, EventState.CONNECTING,
                         EventState.WAITING);

        Restore close = test.start(results::add);

        state.checkNow();

        assertThat(results.size(), is(0));

        state.startCheck(EventState.WAITING, EventState.INCOMPLETE);

        close.close();

        state.checkNow();
    }
}
