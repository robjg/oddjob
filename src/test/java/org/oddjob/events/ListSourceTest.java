package org.oddjob.events;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.events.state.EventState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ListSourceTest {

    private static class IntSource implements InstantEventSource<Integer> {

        @Override
        public Restore subscribe(Consumer<? super InstantEvent<Integer>> consumer) {
            consumer.accept(new WrapperOf<>(1, Instant.now()));
            consumer.accept(new WrapperOf<>(2, Instant.now()));
            return () -> {
            };
        }
    }


    @Test
    public void testListSourceWithAllTwoChildren() throws Exception {

        ListSource<Integer> test = new ListSource<>();

        test.setOf(0, new IntSource());
        test.setOf(1, new IntSource());

        List<InstantEvent<Integer>> results = new ArrayList<>();

        StateSteps stateSteps = new StateSteps(test);
        stateSteps.startCheck(EventState.READY, EventState.CONNECTING,
                              EventState.FIRING, EventState.TRIGGERED,
                              EventState.FIRING, EventState.TRIGGERED,
                              EventState.FIRING, EventState.TRIGGERED);

        test.setTo(results::add);
        test.run();

        stateSteps.checkNow();

        stateSteps.startCheck(EventState.TRIGGERED, EventState.COMPLETE);

        test.stop();

        stateSteps.checkNow();

        assertThat(results.size(), is(3));
        assertThat(EventConversions.toList((CompositeEvent<?>) results.get(0)),
                   is(Arrays.asList(1, 1)));
        assertThat(EventConversions.toList((CompositeEvent<?>) results.get(1)),
                   is(Arrays.asList(2, 1)));
        assertThat(EventConversions.toList((CompositeEvent<?>) results.get(2))
                , is(Arrays.asList(2, 2)));

        stateSteps.startCheck(EventState.COMPLETE, EventState.READY);

        test.hardReset();

        stateSteps.checkNow();

        results.clear();

        stateSteps.startCheck(EventState.READY, EventState.CONNECTING,
                              EventState.FIRING, EventState.TRIGGERED,
                              EventState.FIRING, EventState.TRIGGERED,
                              EventState.FIRING, EventState.TRIGGERED);

        test.setTo(results::add);
        test.run();

        stateSteps.checkNow();

        stateSteps.startCheck(EventState.TRIGGERED, EventState.COMPLETE);

        test.stop();

        stateSteps.checkNow();

        assertThat(results.size(), is(3));
        assertThat(EventConversions.toList((CompositeEvent<?>) results.get(0)),
                   is(Arrays.asList(1, 1)));
        assertThat(EventConversions.toList((CompositeEvent<?>) results.get(1)),
                   is(Arrays.asList(2, 1)));
        assertThat(EventConversions.toList((CompositeEvent<?>) results.get(2)),
                   is(Arrays.asList(2, 2)));
    }

    @Test
    public void testEventsReceivedAfterSubscribe() throws Exception {

        AtomicReference<Consumer<? super String>> c1 = new AtomicReference<>();

        EventSource<String> es1 = consumer -> {
            c1.set(consumer);
            return () -> c1.set(null);
        };

        AtomicReference<Consumer<? super String>> c2 = new AtomicReference<>();

        EventSource<String> es2 = consumer -> {
            c2.set(consumer);
            return () -> c2.set(null);
        };

        ListSource<String> test = new ListSource<>();
        test.setOf(0, es1);
        test.setOf(1, es2);

        StateSteps stateSteps = new StateSteps(test);
        stateSteps.startCheck(EventState.READY, EventState.CONNECTING, EventState.WAITING);

        List<CompositeEvent<String>> results = new ArrayList<>();

        test.setTo(results::add);
        test.run();

        stateSteps.checkNow();

        stateSteps.startCheck(EventState.WAITING, EventState.FIRING, EventState.TRIGGERED);

        c1.get().accept("apple");

        assertThat(results, Matchers.empty());

        Thread.sleep(2L);

        c2.get().accept("orange");

        stateSteps.checkNow();

        CompositeEvent<String> result1  = results.get(0);
        assertThat(result1.getOfs(), hasItems("apple", "orange"));
        // the last event received.
        assertThat(result1.getOf(), is("orange"));

        stateSteps.startCheck(EventState.TRIGGERED, EventState.FIRING, EventState.TRIGGERED);

        c2.get().accept("pear");

        stateSteps.checkNow();

        CompositeEvent<String> result2  = results.get(1);
        assertThat(result2.getOfs(), hasItems("apple", "pear"));

        c1.get().accept("grape");

        CompositeEvent<String> result3  = results.get(2);
        assertThat(result3.getOfs(), hasItems("grape", "pear"));

        stateSteps.startCheck(EventState.TRIGGERED, EventState.COMPLETE);

        test.stop();

        stateSteps.checkNow();

        assertThat(c1.get(), nullValue());
        assertThat(c2.get(), nullValue());

        stateSteps.startCheck(EventState.COMPLETE, EventState.READY);

        test.hardReset();

        stateSteps.checkNow();

        stateSteps.startCheck(EventState.READY, EventState.CONNECTING, EventState.WAITING);

        test.setTo(results::add);
        test.run();

        stateSteps.checkNow();

        stateSteps.startCheck(EventState.WAITING);

        c1.get().accept("banana");

        assertThat(results.size(), is(3));
        stateSteps.checkNow();

        stateSteps.startCheck(EventState.WAITING, EventState.FIRING, EventState.TRIGGERED);

        c2.get().accept("kiwi");

        stateSteps.checkNow();

        CompositeEvent<String> result4  = results.get(3);
        assertThat(result4.getOfs(), hasItems("banana", "kiwi"));

        stateSteps.startCheck(EventState.TRIGGERED, EventState.COMPLETE);

        test.stop();

        stateSteps.checkNow();

        assertThat(c1.get(), nullValue());
        assertThat(c2.get(), nullValue());
    }

    @Test
    public void testExample() throws ArooaConversionException, InterruptedException, FailedToStopException {

        File file = new File(Objects.requireNonNull(
                getClass().getResource("ListSourceExample.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);
        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.ACTIVE));

        StateSteps oddjobState = new StateSteps(oddjob);
        oddjobState.startCheck( ParentState.ACTIVE, ParentState.STARTED);

        OddjobLookup lookup = new OddjobLookup(oddjob);

        StateSteps job3State = new StateSteps(lookup.lookup("job3", Stateful.class));
        job3State.startCheck(JobState.READY,
                JobState.EXECUTING,
                JobState.COMPLETE);

        lookup.lookup("job1", Runnable.class).run();
        lookup.lookup("job2", Runnable.class).run();

        job3State.checkWait();
        oddjobState.checkWait();

        Object when = lookup.lookup("when");

        ((Stoppable) when).stop();
        ((Resettable) when).hardReset();

        oddjobState.startCheck(ParentState.READY,
                ParentState.ACTIVE,
                ParentState.STARTED);

        job3State.startCheck(JobState.READY,
                JobState.EXECUTING,
                JobState.COMPLETE);

        ((Runnable) when).run();

        oddjobState.checkWait();

        job3State.checkNow();

        oddjob.stop();
        oddjob.destroy();
    }
}