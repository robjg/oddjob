package org.oddjob.events;


import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.beanbus.Destination;
import org.oddjob.beanbus.Outbound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventSourceAdaptorTest {

    public static class OurOutbound implements Outbound, Runnable, Stoppable {

        boolean running;

        Consumer<? super String> consumer;

        @Override
        public void run() {
            this.running = true;
        }

        @Override
        public void stop() throws FailedToStopException {
            this.running = false;
        }

        @Override
        public void setTo(Consumer destination) {

            assertThat(this.consumer, nullValue());
            assertThat(destination, notNullValue());

            this.consumer = destination;
        }
    }


    @Test
    public void testSubscribeUnsubscribe() throws Exception {

        OurOutbound ourOutbound = new OurOutbound();

        EventSource<String> eventSource =
                EventSourceAdaptor
                        .<String>maybeEventSourceFrom(ourOutbound, mock(ArooaSession.class))
                        .get();

        List<String> results = new ArrayList<>();

        AutoCloseable autoCloseable =
                eventSource.subscribe(results::add);

        ourOutbound.consumer.accept("First");

        assertThat(ourOutbound.running, is(true));

        assertThat(results, contains("First"));

        autoCloseable.close();

        assertThat(ourOutbound.running, is(false));
    }

    public static class OurDestination implements Runnable, Stoppable {

        boolean running;

        Consumer<? super String> consumer;

        @Override
        public void run() {
            this.running = true;
        }

        @Override
        public void stop() throws FailedToStopException {
            this.running = false;
        }

        @Destination
        public void setTo(Consumer destination) {

            assertThat(this.consumer, nullValue());
            assertThat(destination, notNullValue());

            this.consumer = destination;
        }
    }


    @Test
    public void testSubscribeUnsubscribeDestination() throws Exception {

        OurDestination ourDestination = new OurDestination();

        ArooaSession arooaSession = new StandardArooaSession();

        ArooaContext arooaContext = mock(ArooaContext.class);
        when(arooaContext.getSession()).thenReturn(arooaSession);

        arooaSession.getComponentPool().registerComponent(
                ComponentTrinity.withComponent(ourDestination).noProxy().andArooaContext(arooaContext), "foo");

        EventSource<String> eventSource =
                EventSourceAdaptor
                        .<String>maybeEventSourceFrom(ourDestination, arooaSession)
                        .get();

        List<String> results = new ArrayList<>();

        AutoCloseable autoCloseable =
                eventSource.subscribe(results::add);

        ourDestination.consumer.accept("First");

        assertThat(ourDestination.running, is(true));

        assertThat(results, contains("First"));

        autoCloseable.close();

        assertThat(ourDestination.running, is(false));
    }
}