package org.oddjob.events.example;

import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PricingWhenExampleTest {

    private static final Logger logger = LoggerFactory.getLogger(PricingWhenExampleTest.class);

    @Test
    public void testInOddjob() throws InterruptedException, FailedToStopException {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(getClass().getResource("PricingWhenExample.xml").getFile()));

        logger.info("** Loading.");

        oddjob.load();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.READY));

        StateSteps oddjobState = new StateSteps(oddjob);
        oddjobState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.STARTED);

        logger.info("** Starting.");

        oddjob.run();

        oddjobState.checkWait();

        logger.info("** Stopping.");

        oddjob.stop();

        oddjob.destroy();
    }
}
