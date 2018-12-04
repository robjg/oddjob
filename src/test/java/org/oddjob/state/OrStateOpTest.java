package org.oddjob.state;

import org.junit.Test;
import org.oddjob.OjTestCase;

public class OrStateOpTest extends OjTestCase {

    static class JobEvents {

        static StateEvent READY = ConstStateful.event(JobState.READY);

        static StateEvent EXECUTING = ConstStateful.event(JobState.EXECUTING);

        static StateEvent EXCEPTION = ConstStateful.exception(JobState.EXCEPTION, new RuntimeException());

        static StateEvent INCOMPLETE = ConstStateful.event(JobState.INCOMPLETE);

        static StateEvent COMPLETE = ConstStateful.event(JobState.COMPLETE);

        static StateEvent DESTROYED = ConstStateful.event(JobState.DESTROYED);
    }

    @Test
    public void testOrNoStates() {

        OrStateOp test = new OrStateOp();

        assertEquals(null, test.evaluate());
    }

    @Test
    public void testOrOneStates() {

        OrStateOp test = new OrStateOp();

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
    public void testOr() {

        OrStateOp test = new OrStateOp();

        assertEquals(ParentState.READY,
                test.evaluate(JobEvents.READY, JobEvents.READY).getState());

        assertEquals(ParentState.COMPLETE,
                test.evaluate(JobEvents.COMPLETE, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.COMPLETE,
                test.evaluate(JobEvents.COMPLETE, JobEvents.READY).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.COMPLETE, JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.COMPLETE, JobEvents.EXECUTING).getState());
    }
}
