package org.oddjob.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.oddjob.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExecutorThrottleTypeTest {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorThrottleTypeTest.class);

    @BeforeEach
    void setUp(TestInfo testInfo) throws Exception {
        logger.info("---------------------- {}-----------------------", testInfo.getDisplayName());
    }

    private static class Capture implements StructuralListener, StateListener {

        Set<Stateful> ready = new HashSet<>();

        Set<Stateful> executing = new HashSet<>();

        Set<Stateful> complete = new HashSet<>();

        @Override
        public void jobStateChange(StateEvent event) {
            logger.info("Received: {}", event);

            synchronized (this) {
                switch ((JobState) event.getState()) {
                    case READY:
                        ready.add(event.getSource());
                        break;
                    case EXECUTING:
                        ready.remove(event.getSource());
                        executing.add(event.getSource());
                        break;
                    case COMPLETE:
                        executing.remove(event.getSource());
                        complete.add(event.getSource());
                        break;
                    default:
                        throw new RuntimeException("Unexpected " +
                                event.getState());
                }

                this.notifyAll();
            }
        }

        synchronized int getReadyCount() {
            return ready.size();
        }

        synchronized int getCompleteCount() {
            return complete.size();
        }

        @SuppressWarnings("SameParameterValue")
        synchronized void waitForExecuting(int count) throws InterruptedException {
            while (executing.size() < count) {
                logger.info("Waiting for {} EXECUTING.", count);
                wait();
            }
        }

        synchronized void waitForComplete(int count) throws InterruptedException {
            while (complete.size() < count) {
                logger.info("Waiting for {} COMPLETE.", count);
                wait();
            }

        }

        @Override
        public void childAdded(StructuralEvent event) {
            ((Stateful) event.getChild()).addStateListener(this);
        }

        @Override
        public void childRemoved(StructuralEvent event) {
            ((Stateful) event.getChild()).removeStateListener(this);
        }

    }

    @Test
    public void testThrottleInParallel() throws InterruptedException, ArooaPropertyException, ArooaConversionException, FailedToStopException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/scheduling/ExecutorThrottleInParallel.xml",
                getClass().getClassLoader()));

        StateSteps oddjobState = new StateSteps(oddjob);

        oddjobState.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        oddjob.run();

        oddjobState.checkNow();

        oddjobState.startCheck(ParentState.ACTIVE,
                ParentState.COMPLETE);

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Structural parallel = lookup.lookup("parallel", Structural.class);

        Capture capture = new Capture();

        parallel.addStructuralListener(capture);

        capture.waitForExecuting(2);

        assertEquals(2, capture.getReadyCount());

        ((Stoppable) capture.executing.iterator().next()).stop();

        capture.waitForComplete(1);

        capture.waitForExecuting(2);

        ((Stoppable) capture.executing.iterator().next()).stop();

        capture.waitForComplete(2);

        capture.waitForExecuting(2);

        ((Stoppable) parallel).stop();

        capture.waitForComplete(4);

        oddjobState.checkWait();

        oddjob.destroy();
    }

    @Test
    public void testStopParallel() throws InterruptedException, ArooaPropertyException, ArooaConversionException, FailedToStopException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/scheduling/ExecutorThrottleInParallel.xml",
                getClass().getClassLoader()));

        // Must be added before StateSteps to ensure this
        // gets State first.
        Capture capture = new Capture();
        oddjob.load();
        OddjobLookup lookup = new OddjobLookup(oddjob);
        Structural parallel = lookup.lookup("parallel", Structural.class);
        parallel.addStructuralListener(capture);

        // Now we can add state steps. Can't watch Oddjob because it adds StateListener to parallel before
        // we can add Capture.
        StateSteps parallelState = new StateSteps((Stateful) parallel);

        parallelState.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        oddjob.run();

        parallelState.checkNow();

        parallelState.startCheck(ParentState.ACTIVE, ParentState.READY);

        capture.waitForExecuting(2);

        ((Stoppable) parallel).stop();

        parallelState.checkWait();

        assertEquals(2, capture.getCompleteCount());
        assertEquals(2, capture.getReadyCount());

        assertThat(oddjob.lastStateEvent().getState().isReady(), is(true));

        oddjob.destroy();
    }

    @Test
    public void testThrottleShared() throws InterruptedException, ArooaPropertyException, ArooaConversionException, FailedToStopException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/scheduling/ExecutorThrottleShared.xml",
                getClass().getClassLoader()));

        StateSteps oddjobState = new StateSteps(oddjob);

        oddjobState.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        oddjob.run();

        oddjobState.checkNow();

        oddjobState.startCheck(ParentState.ACTIVE,
                ParentState.COMPLETE);

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Structural parallelOne = lookup.lookup("parallel-1", Structural.class);
        Structural parallelTwo = lookup.lookup("parallel-2", Structural.class);

        Capture capture = new Capture();

        parallelOne.addStructuralListener(capture);
        parallelTwo.addStructuralListener(capture);

        capture.waitForExecuting(2);

        assertEquals(2, capture.ready.size());

        ((Stoppable) capture.executing.iterator().next()).stop();

        capture.waitForComplete(1);

        capture.waitForExecuting(2);

        ((Stoppable) capture.executing.iterator().next()).stop();

        capture.waitForComplete(2);

        capture.waitForExecuting(2);

        ((Stoppable) parallelOne).stop();
        ((Stoppable) parallelTwo).stop();

        capture.waitForComplete(4);

        oddjobState.checkWait();

        oddjob.destroy();
    }
}
