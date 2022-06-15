/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.jobs;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.StateIcons;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.StateListener;

import static org.hamcrest.Matchers.not;

/**
 * @author Rob Gordon.
 */
public class JobStateTest extends OjTestCase {

    JobState jobState;
    String iconId;

    StateListener stateListener = event -> JobStateTest.this.jobState = (JobState) event.getState();

    IconListener iconListener = event -> JobStateTest.this.iconId = event.getIconId();

    @Test
    public void testException() {
        final FlagState j = new FlagState();
        j.setName("Exception Test");
        j.setState(JobState.EXCEPTION);

        j.addStateListener(stateListener);
        j.addIconListener(iconListener);
        assertSame("job state not READY.", jobState, JobState.READY);

        j.run();

        assertSame("job state not EXCEPTION.", jobState, JobState.EXCEPTION);
        assertEquals("Icon not EXCEPTION", iconId, IconHelper.EXCEPTION);
    }

    @Test
    public void testNotComplete() {
        final FlagState j = new FlagState();
        j.setName("Test Not Complete");
        j.setState(JobState.INCOMPLETE);

        j.addStateListener(stateListener);
        j.addIconListener(iconListener);
        assertSame("job state not READY.", jobState, JobState.READY);
        j.run();

        assertSame("job state not NOT COMPLETE.", jobState, JobState.INCOMPLETE);
        assertEquals("Icon not NOT COMPLETE", iconId, IconHelper.NOT_COMPLETE);
    }

    @Test
    public void testComplete() {
        final FlagState j = new FlagState();
        j.setName("Test Complete");
        j.setState(JobState.COMPLETE);

        j.addStateListener(stateListener);
        j.addIconListener(iconListener);
        assertSame("job state not READY.", jobState, JobState.READY);

        j.run();

        assertSame("job state not COMPLETE.", jobState, JobState.COMPLETE);
        assertEquals("Icon not COMPLETE", iconId, IconHelper.COMPLETE);
    }

    @Test
    public void testIconsForAllStates() {

        for (JobState jobState : JobState.values()) {
            assertThat(jobState.name(), StateIcons.iconFor(jobState), not(IconHelper.NULL));
        }
    }
}
