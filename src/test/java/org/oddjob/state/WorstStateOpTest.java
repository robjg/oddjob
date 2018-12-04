package org.oddjob.state;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.scheduling.state.TimerState;

public class WorstStateOpTest extends OjTestCase {

    static class JobEvents {

        static StateEvent READY = ConstStateful.event(JobState.READY);

        static StateEvent EXECUTING = ConstStateful.event(JobState.EXECUTING);

        static StateEvent EXCEPTION = ConstStateful.exception(JobState.EXCEPTION, new RuntimeException());

        static StateEvent INCOMPLETE = ConstStateful.event(JobState.INCOMPLETE);

        static StateEvent COMPLETE = ConstStateful.event(JobState.COMPLETE);

        static StateEvent DESTROYED = ConstStateful.event(JobState.DESTROYED);
    }

    static class ServiceEvents {

        static StateEvent STARTABLE = ConstStateful.event(ServiceState.STARTABLE);

        static StateEvent STARTING = ConstStateful.event(ServiceState.STARTING);

        static StateEvent STARTED = ConstStateful.event(ServiceState.STARTED);

        static StateEvent EXCEPTION = ConstStateful.exception(ServiceState.EXCEPTION, new RuntimeException());

        static StateEvent STOPPED = ConstStateful.event(ServiceState.STOPPED);
    }

    static class TimerEvents {

        static StateEvent STARTABLE = ConstStateful.event(TimerState.STARTABLE);

        static StateEvent STARTING = ConstStateful.event(TimerState.STARTING);

        static StateEvent STARTED = ConstStateful.event(TimerState.STARTED);

        static StateEvent EXCEPTION = ConstStateful.exception(TimerState.EXCEPTION, new RuntimeException());

        static StateEvent INCOMPLETE = ConstStateful.event(TimerState.INCOMPLETE);

        static StateEvent COMPLETE = ConstStateful.event(TimerState.COMPLETE);
    }

    @Test
    public void testEvaluateSingleJobOp() {

        WorstStateOp test = new WorstStateOp();

        assertEquals(ParentState.READY,
                test.evaluate(JobEvents.READY).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.EXECUTING).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.COMPLETE,
                test.evaluate(JobEvents.COMPLETE).getState());

    }

    @Test
    public void testAssociative() {

        WorstStateOp test = new WorstStateOp();

        StateEvent[] values =  new StateEvent[] {
                JobEvents.READY, JobEvents.EXECUTING,
                JobEvents.COMPLETE, JobEvents.INCOMPLETE, JobEvents.EXCEPTION
        };

        for (int i = 0; i < values.length - 1; ++i) {
            for (int j = 0; j < values.length - 1; ++j) {
                StateEvent oneWay = test.evaluate(values[i], values[j]);
                StateEvent otherWay = test.evaluate(values[j], values[i]);

                assertSame("Failed on i=" + i + ", j = " + j,
                        oneWay.getState(), otherWay.getState());
            }
        }
    }

    @Test
    public void testEvaluateSingleServiceOp() {

        WorstStateOp test = new WorstStateOp();

        assertEquals(ParentState.READY,
                test.evaluate(ServiceEvents.STARTABLE).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STARTING).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.EXCEPTION).getState());

        assertEquals(ParentState.STARTED,
                test.evaluate(ServiceEvents.STARTED).getState());

        assertEquals(ParentState.COMPLETE,
                test.evaluate(ServiceEvents.STOPPED).getState());

    }

    @Test
    public void testEvaluateTwoJobOps() {

        WorstStateOp test = new WorstStateOp();

        assertEquals(ParentState.READY,
                test.evaluate(JobEvents.READY, JobEvents.READY).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.READY, JobEvents.EXECUTING).getState());

        assertEquals(ParentState.READY,
                test.evaluate(JobEvents.READY, JobEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.READY, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.READY, JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.EXECUTING, JobEvents.READY).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.EXECUTING, JobEvents.EXECUTING).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.EXECUTING, JobEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.EXECUTING, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXECUTING, JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.INCOMPLETE, JobEvents.READY).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.INCOMPLETE, JobEvents.EXECUTING).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.INCOMPLETE, JobEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.INCOMPLETE, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.INCOMPLETE, JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.READY,
                test.evaluate(JobEvents.COMPLETE, JobEvents.READY).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.COMPLETE, JobEvents.EXECUTING).getState());

        assertEquals(ParentState.COMPLETE,
                test.evaluate(JobEvents.COMPLETE, JobEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.COMPLETE, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.COMPLETE, JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION, JobEvents.READY).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION, JobEvents.EXECUTING).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION, JobEvents.COMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION, JobEvents.EXCEPTION).getState());
    }

    @Test
    public void testEvaluateTwoServiceOps() {

        WorstStateOp test = new WorstStateOp();

        assertEquals(ParentState.READY,
                test.evaluate(ServiceEvents.STARTABLE, ServiceEvents.STARTABLE).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STARTABLE, ServiceEvents.STARTING).getState());

        assertEquals(ParentState.STARTED,
                test.evaluate(ServiceEvents.STARTABLE, ServiceEvents.STARTED).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.STARTABLE, ServiceEvents.EXCEPTION).getState());

        assertEquals(ParentState.READY,
                test.evaluate(ServiceEvents.STARTABLE, ServiceEvents.STOPPED).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STARTING, ServiceEvents.STARTABLE).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STARTING, ServiceEvents.STARTING).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STARTING, ServiceEvents.STARTED).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.STARTING, ServiceEvents.EXCEPTION).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STARTING, ServiceEvents.STARTABLE).getState());

        assertEquals(ParentState.STARTED,
                test.evaluate(ServiceEvents.STARTED, ServiceEvents.STOPPED).getState());

        assertEquals(ParentState.STARTED,
                test.evaluate(ServiceEvents.STARTED, ServiceEvents.STARTED).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.STARTED, ServiceEvents.EXCEPTION).getState());

        assertEquals(ParentState.STARTED,
                test.evaluate(ServiceEvents.STARTED, ServiceEvents.STOPPED).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.EXCEPTION, ServiceEvents.STARTABLE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.EXCEPTION, ServiceEvents.STARTING).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.EXCEPTION, ServiceEvents.STARTED).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.EXCEPTION, ServiceEvents.EXCEPTION).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.EXCEPTION, ServiceEvents.STOPPED).getState());

        assertEquals(ParentState.READY,
                test.evaluate(ServiceEvents.STOPPED, ServiceEvents.STARTABLE).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STOPPED, ServiceEvents.STARTING).getState());

        assertEquals(ParentState.STARTED,
                test.evaluate(ServiceEvents.STOPPED, ServiceEvents.STARTED).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.STOPPED, ServiceEvents.EXCEPTION).getState());

        assertEquals(ParentState.COMPLETE,
                test.evaluate(ServiceEvents.STOPPED, ServiceEvents.STOPPED).getState());
    }

    @Test
    public void testEvaluateServiceStateAndJobState() {

        WorstStateOp test = new WorstStateOp();

        assertEquals(ParentState.READY,
                test.evaluate(ServiceEvents.STARTABLE, JobEvents.READY).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STARTABLE, JobEvents.EXECUTING).getState());

        assertEquals(ParentState.READY,
                test.evaluate(ServiceEvents.STARTABLE, JobEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(ServiceEvents.STARTABLE, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.STARTABLE, JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STARTING, JobEvents.READY).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STARTING, JobEvents.EXECUTING).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STARTING, JobEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(ServiceEvents.STARTING, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.STARTING, JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.STARTED,
                test.evaluate(ServiceEvents.STARTED, JobEvents.READY).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STARTED, JobEvents.EXECUTING).getState());

        assertEquals(ParentState.STARTED,
                test.evaluate(ServiceEvents.STARTED, JobEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(ServiceEvents.STARTED, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.STARTED, JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.EXCEPTION, JobEvents.READY).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.EXCEPTION, JobEvents.EXECUTING).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.EXCEPTION, JobEvents.COMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.EXCEPTION, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.EXCEPTION, JobEvents.EXCEPTION).getState());

        assertEquals(ParentState.READY,
                test.evaluate(ServiceEvents.STOPPED, JobEvents.READY).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(ServiceEvents.STOPPED, JobEvents.EXECUTING).getState());

        assertEquals(ParentState.COMPLETE,
                test.evaluate(ServiceEvents.STOPPED, JobEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(ServiceEvents.STOPPED, JobEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(ServiceEvents.STOPPED, JobEvents.EXCEPTION).getState());
    }

    @Test
    public void testEvaluateJobStateAndTimerState() {

        WorstStateOp test = new WorstStateOp();

        assertEquals(ParentState.READY,
                test.evaluate(JobEvents.READY, TimerEvents.STARTABLE).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.READY, TimerEvents.STARTING).getState());

        assertEquals(ParentState.STARTED,
                test.evaluate(JobEvents.READY, TimerEvents.STARTED).getState());

        assertEquals(ParentState.READY,
                test.evaluate(JobEvents.READY, TimerEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.READY, TimerEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.READY, TimerEvents.EXCEPTION).getState());

        //

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.EXECUTING, TimerEvents.STARTABLE).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.EXECUTING, TimerEvents.STARTING).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.EXECUTING, TimerEvents.STARTED).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.EXECUTING, TimerEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.EXECUTING, TimerEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXECUTING, TimerEvents.EXCEPTION).getState());

        //

        assertEquals(ParentState.READY,
                test.evaluate(JobEvents.COMPLETE, TimerEvents.STARTABLE).getState());

        assertEquals(ParentState.ACTIVE,
                test.evaluate(JobEvents.COMPLETE, TimerEvents.STARTING).getState());

        assertEquals(ParentState.STARTED,
                test.evaluate(JobEvents.COMPLETE, TimerEvents.STARTED).getState());

        assertEquals(ParentState.COMPLETE,
                test.evaluate(JobEvents.COMPLETE, TimerEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.COMPLETE, TimerEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.COMPLETE, TimerEvents.EXCEPTION).getState());

        //

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.INCOMPLETE, TimerEvents.STARTABLE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.INCOMPLETE, TimerEvents.STARTING).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.INCOMPLETE, TimerEvents.STARTED).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.INCOMPLETE, TimerEvents.COMPLETE).getState());

        assertEquals(ParentState.INCOMPLETE,
                test.evaluate(JobEvents.INCOMPLETE, TimerEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.INCOMPLETE, TimerEvents.EXCEPTION).getState());

        //

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION, TimerEvents.STARTABLE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION, TimerEvents.STARTING).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION, TimerEvents.STARTED).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION, TimerEvents.COMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION, TimerEvents.INCOMPLETE).getState());

        assertEquals(ParentState.EXCEPTION,
                test.evaluate(JobEvents.EXCEPTION, TimerEvents.EXCEPTION).getState());

    }

    @Test
    public void testDestroyed() {

        WorstStateOp test = new WorstStateOp();

        try {
            test.evaluate(JobEvents.EXECUTING, JobEvents.DESTROYED);
            fail("Should fail");
        } catch (IllegalStateException e) {
            // expected.
        }
    }
}
