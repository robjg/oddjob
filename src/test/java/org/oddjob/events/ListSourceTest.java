package org.oddjob.events;

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
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ListSourceTest {

    private static class IntSource implements EventSource<Integer> {

        @Override
        public Restore start(Consumer<? super EventOf<Integer>> consumer) {
            consumer.accept(new WrapperOf<>(1, Instant.now()));
            consumer.accept(new WrapperOf<>(2, Instant.now()));
            return () -> {
            };
        }
    }


    @Test
    public void testListSourceWithAllTwoChildren() throws Exception {

        ListSource<Integer> test = new ListSource<>();

        test.setChild(0, new IntSource());
        test.setChild(1, new IntSource());

        List<EventOf<Integer>> results = new ArrayList<>();

        StateSteps stateSteps = new StateSteps(test);
        stateSteps.startCheck(EventState.READY, EventState.CONNECTING,
                              EventState.FIRING, EventState.TRIGGERED,
                              EventState.FIRING, EventState.TRIGGERED,
                              EventState.FIRING, EventState.TRIGGERED);

        Restore restore = test.start(results::add);

        stateSteps.checkNow();

        stateSteps.startCheck(EventState.TRIGGERED, EventState.COMPLETE);

        restore.close();

        stateSteps.checkNow();

        assertThat(results.size(), is(3));
        assertThat(EventConversions.toList((CompositeEvent) results.get(0)),
                   is(Arrays.asList(1, 1)));
        assertThat(EventConversions.toList((CompositeEvent) results.get(1)),
                   is(Arrays.asList(2, 1)));
        assertThat(EventConversions.toList((CompositeEvent) results.get(2))
                , is(Arrays.asList(2, 2)));

        stateSteps.startCheck(EventState.COMPLETE, EventState.READY);

        test.hardReset();

        stateSteps.checkNow();

        results.clear();

        stateSteps.startCheck(EventState.READY, EventState.CONNECTING,
                              EventState.FIRING, EventState.TRIGGERED,
                              EventState.FIRING, EventState.TRIGGERED,
                              EventState.FIRING, EventState.TRIGGERED);

        restore = test.start(results::add);

        stateSteps.checkNow();

        stateSteps.startCheck(EventState.TRIGGERED, EventState.COMPLETE);

        restore.close();

        stateSteps.checkNow();

        assertThat(results.size(), is(3));
        assertThat(EventConversions.toList((CompositeEvent) results.get(0)),
                   is(Arrays.asList(1, 1)));
        assertThat(EventConversions.toList((CompositeEvent) results.get(1)),
                   is(Arrays.asList(2, 1)));
        assertThat(EventConversions.toList((CompositeEvent) results.get(2)),
                   is(Arrays.asList(2, 2)));
    }

    public void testExample() throws ArooaConversionException, InterruptedException, FailedToStopException {

        File file = new File(getClass().getResource("ListSourceExample.xml").getFile());

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
        ((Resetable) when).hardReset();

        oddjobState.startCheck(ParentState.READY,
                ParentState.ACTIVE,
                ParentState.STARTED,
                ParentState.ACTIVE, // This shouldn't happen.
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