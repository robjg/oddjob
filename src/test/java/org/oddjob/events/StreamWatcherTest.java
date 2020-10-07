package org.oddjob.events;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StreamWatcherTest {

    @Test
    public void testExample() throws ArooaConversionException, InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(getClass().getResource("StreamWatcherExample.xml").getFile()));

        StateSteps states = new StateSteps(oddjob);

        states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        oddjob.run();

        states.checkWait();

        OddjobLookup lookup = new OddjobLookup(oddjob);

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