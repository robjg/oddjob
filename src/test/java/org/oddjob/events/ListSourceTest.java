package org.oddjob.events;

import org.junit.Test;
import org.oddjob.events.state.EventState;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Restore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ListSourceTest {

    private static class IntSource implements EventSource<Integer> {

        @Override
        public Restore start(Consumer<? super Integer> consumer) {
            consumer.accept(1);
            consumer.accept(2);
            return () -> {};
        }
    }


    @Test
    public void testListSourceWithAllTwoChildren() throws Exception {

        ListSource<Integer> test = new ListSource<>();

        test.setChild(0, new IntSource());
        test.setChild(1, new IntSource());

        List<List<Integer>> results = new ArrayList<>();

        StateSteps stateSteps = new StateSteps(test);
        stateSteps.startCheck(EventState.READY, EventState.CONNECTING,
                EventState.FIRING, EventState.TRIGGERED,
                EventState.FIRING, EventState.TRIGGERED );

        Restore restore = test.start(results::add);

        stateSteps.checkNow();

        stateSteps.startCheck(EventState.TRIGGERED, EventState.COMPLETE);

        restore.close();

        stateSteps.checkNow();

        assertThat(results.size(), is(2));
        assertThat(results.get(0), is( Arrays.asList(2, 1)));
        assertThat(results.get(1), is( Arrays.asList(2, 2)));

        stateSteps.startCheck(EventState.COMPLETE, EventState.READY);

        test.hardReset();

        stateSteps.checkNow();

        results.clear();

        stateSteps.startCheck(EventState.READY, EventState.CONNECTING,
                EventState.FIRING, EventState.TRIGGERED,
                EventState.FIRING, EventState.TRIGGERED );

        restore = test.start(results::add);

        stateSteps.checkNow();

        stateSteps.startCheck(EventState.TRIGGERED, EventState.COMPLETE);

        restore.close();

        stateSteps.checkNow();

        assertThat(results.size(), is(2));
        assertThat(results.get(0), is( Arrays.asList(2, 1)));
        assertThat(results.get(1), is( Arrays.asList(2, 2)));
    }
}