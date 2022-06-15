package org.oddjob.events;


import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

import java.io.File;
import java.util.Objects;
import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OutboundEventSourceTest {

    public static class OnlyThree implements Predicate<Integer> {

        @Override
        public boolean test(Integer integer) {
            return integer != null && integer == 3;
        }
    }

    @Test
    public void testInOddjob() throws ArooaConversionException, InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(Objects.requireNonNull(getClass()
                .getResource("OutboundEventSourceExample.xml")).getFile()));

        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Stateful trigger = lookup.lookup("trigger", Stateful.class);

        StateSteps states = new StateSteps(trigger);

        states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE, ParentState.COMPLETE);

        oddjob.run();

        states.checkWait();

        String result = lookup.lookup("result.text", String.class);

        assertThat(result, is("Result: 3"));

        oddjob.destroy();
    }
}