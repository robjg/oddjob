package org.oddjob.state;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GenericStateTest {

    @Test
    public void createFromJobState() {


        State test = GenericState.from(JobState.COMPLETE);

        assertThat(test.toString(), is("COMPLETE"));

        assertThat(test.isReady(), is(false));
        assertThat(test.isComplete(), is(true));


        test = GenericState.from(ParentState.STARTED);

        assertThat(test.toString(), is("STARTED"));

        assertThat(test.isReady(), is(false));
        assertThat(test.isExecuting(), is(false));
        assertThat(test.isStoppable(), is(true));
        assertThat(test.isComplete(), is(true));
        assertThat(test.isIncomplete(), is(false));
        assertThat(test.isException(), is(false));
        assertThat(test.isDestroyed(), is(false));
    }

    @Test
    public void testHashCodeEquals() {

        State test1 = GenericState.from(JobState.COMPLETE);

        State test2 = GenericState.from(JobState.COMPLETE);

        assertThat(test1, is(test2));

        assertThat(test1.hashCode(), is(test2.hashCode()));

        State test3 = GenericState.from(ServiceState.STOPPED);

        assertThat(test1, Matchers.not(test3));
    }

    @Test
    public void testEquivalentTo() {

        State test1 = GenericState.from(JobState.COMPLETE);

        State test3 = GenericState.from(ServiceState.STOPPED);

        assertThat(GenericState.statesEquivalent(test1, test3), Matchers.is(true));
    }
}