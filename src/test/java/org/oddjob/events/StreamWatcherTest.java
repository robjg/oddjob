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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StreamWatcherTest {

    @Test
    public void testExample() throws ArooaConversionException, InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(Objects.requireNonNull(getClass()
                .getResource("StreamWatcherExample.xml")).getFile()));

        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Stateful trigger = lookup.lookup("trigger", Stateful.class);

        StateSteps states = new StateSteps(trigger);

        states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        oddjob.run();

        states.checkWait();

        states.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);

        Runnable job1 = lookup.lookup("job1", Runnable.class);
        job1.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.ACTIVE));

        Runnable job2 = lookup.lookup("job2", Runnable.class);
        job2.run();

        states.checkWait();

        String text = lookup.lookup("echo.text", String.class);

        assertThat(text, is("Found Apple"));
    }

}