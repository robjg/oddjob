package org.oddjob.state.expr;

import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

import java.io.File;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StateExpressionExamplesTest {

    @Test
    public void testLastTimeExample() throws FailedToStopException, ArooaConversionException, InterruptedException {

        File file = new File(Objects.requireNonNull(getClass()
                        .getResource("StateExpressionTimeExample.xml"))
                .getFile());

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
        job3State.startCheck(JobState.COMPLETE);

        ((Runnable) when).run();

        oddjobState.checkWait();

        job3State.checkNow();

        oddjob.stop();
        oddjob.destroy();
    }
}
