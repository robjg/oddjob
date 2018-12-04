package org.oddjob.state;

import org.junit.Test;

import org.oddjob.OjTestCase;

public class AndStateOpTest extends OjTestCase {

    static class JobEvents {

        static StateEvent READY = ConstStateful.event(JobState.READY);

        static StateEvent EXECUTING = ConstStateful.event(JobState.EXECUTING);

        static StateEvent EXCEPTION = ConstStateful.exception(JobState.EXCEPTION, new RuntimeException());

        static StateEvent INCOMPLETE = ConstStateful.event(JobState.INCOMPLETE);

        static StateEvent COMPLETE = ConstStateful.event(JobState.COMPLETE);

        static StateEvent DESTROYED = ConstStateful.event(JobState.DESTROYED);
    }

    @Test
    public void testAndNoStates() {

        AndStateOp test = new AndStateOp();

        assertEquals(null, test.evaluate());
    }

    @Test
    public void testAndOneStates() {

        AndStateOp test = new AndStateOp();

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.COMPLETE,
                test.evaluate(JobEvents.COMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.EXECUTING).getState());
    }

    @Test
    public void testAndTwoStates() {

        AndStateOp test = new AndStateOp();

        assertEquals(ParentState.READY,
                test.evaluate(JobEvents.COMPLETE, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.COMPLETE,
                test.evaluate(JobEvents.COMPLETE, JobEvents.COMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.COMPLETE, JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.COMPLETE, JobEvents.EXECUTING).getState());
    }
}
