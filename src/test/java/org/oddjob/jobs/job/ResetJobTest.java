package org.oddjob.jobs.job;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OjTestCase;
import org.oddjob.Resettable;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.is;

public class ResetJobTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(
            ResetJobTest.class);

    @Before
    public void setUp() throws Exception {

        logger.info("-------------------------  " + getName() +
                "  ------------------------");
    }

    @Test
    public void testReset() {

        FlagState job = new FlagState(JobState.INCOMPLETE);

        job.run();

        assertEquals(JobState.INCOMPLETE, job.lastStateEvent().getState());

        ResetJob test = new ResetJob();

        test.setJob(job);
        test.run();

        assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
        assertEquals(JobState.READY, job.lastStateEvent().getState());

        job.setState(JobState.COMPLETE);

        job.run();

        assertEquals(JobState.COMPLETE, job.lastStateEvent().getState());

        test.hardReset();
        test.setLevel(ResetActions.HARD);

        test.run();

        assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
        assertEquals(JobState.READY, job.lastStateEvent().getState());

    }

    @Test
    public void testResetForceExample() throws IOException {

        File file = new File(getClass().getResource(
                "ResetForceExample.xml").getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);

        ConsoleCapture console = new ConsoleCapture();

        try (ConsoleCapture.Close closeable = console.captureConsole()) {

            oddjob.run();
        }

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        assertEquals(0, console.getLines().length);

        oddjob.destroy();
    }

    static class SomeResettable implements Resettable {

        private boolean softReset;

        private boolean hardReset;

        @Override
        public boolean softReset() {
            this.softReset = true;
            return true;
        }

        @Override
        public boolean hardReset() {
            this.hardReset = true;
            return false;
        }
    }

    @Test
    public void whenJustResettableThenReset() {

        SomeResettable resettable = new SomeResettable();

        ResetJob resetJob = new ResetJob();
        resetJob.setJob(resettable);
        resetJob.setLevel(ResetActions.HARD);

        resetJob.run();

        MatcherAssert.assertThat(resetJob.lastStateEvent().getState(), is(JobState.COMPLETE));
        MatcherAssert.assertThat(resettable.hardReset, is(true));
        MatcherAssert.assertThat(resettable.softReset, is(false));
    }
}
