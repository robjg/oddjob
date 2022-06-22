package org.oddjob.events.example;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PricingTriggerExampleTest {

    private static final Logger logger = LoggerFactory.getLogger(PricingTriggerExampleTest.class);

    @Test
    public void testInOddjob() throws InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(Objects.requireNonNull(
                getClass().getResource("PricingTriggerExample.xml")).getFile()));

        logger.info("** Loading.");

        oddjob.load();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.READY));

        StateSteps oddjobState = new StateSteps(oddjob);
        oddjobState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.COMPLETE);

        logger.info("** Starting.");

        oddjob.run();

        oddjobState.checkWait();

        oddjob.destroy();
    }
}
