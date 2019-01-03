package org.oddjob.tools;

import org.junit.Test;
import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.state.JobState;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class StateStepsTest {

    static class SomeStateful implements Stateful {

        private final List<StateEvent> states;

        private StateListener stateListener;

        private int index = 0;


        SomeStateful(State... states) {
            this.states = new ArrayList<>();

            for (State state : states ) {
                this.states.add(new StateEvent(this, state));
            }
        }


        @Override
        public void addStateListener(StateListener listener) throws JobDestroyedException {
            if (stateListener != null) {
                throw new IllegalStateException();
            }
            stateListener = listener;
            stateListener.jobStateChange(states.get(index));
        }

        @Override
        public void removeStateListener(StateListener listener) {
            if (stateListener != listener) {
                throw new IllegalStateException();
            }
            stateListener = null;
        }

        @Override
        public StateEvent lastStateEvent() {
            return states.get(index);
        }

        void advance() {
            stateListener.jobStateChange(states.get(++index));
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    @Test
    public void whenExpectedStepsThenOk() {

        SomeStateful stateful = new SomeStateful(JobState.READY,
                JobState.EXECUTING, JobState.COMPLETE);

        StateSteps test = new StateSteps(stateful);
        test.startCheck(JobState.READY,
                JobState.EXECUTING, JobState.COMPLETE);

        stateful.advance();
        stateful.advance();

        test.checkNow();
    }

    @Test
    public void whenMaybeThenOk() {

        SomeStateful stateful = new SomeStateful(JobState.READY,
                JobState.EXECUTING, JobState.COMPLETE);

        StateSteps test = new StateSteps(stateful);
        test.startCheck(StateSteps.definitely(JobState.READY),
                StateSteps.maybe(JobState.EXECUTING),
                StateSteps.definitely(JobState.COMPLETE));

        stateful.advance();
        stateful.advance();

        test.checkNow();
    }

    @Test
    public void whenMaybeMissingOk() {

        SomeStateful stateful = new SomeStateful(JobState.READY,
                JobState.COMPLETE);

        StateSteps test = new StateSteps(stateful);
        test.startCheck(StateSteps.definitely(JobState.READY),
                StateSteps.maybe(JobState.EXECUTING),
                StateSteps.definitely(JobState.COMPLETE));

        stateful.advance();

        test.checkNow();
    }

    @Test
    public void whenNotThenNotEnoughMessageIsOk() {

        SomeStateful stateful = new SomeStateful(JobState.READY,
                JobState.EXECUTING);

        StateSteps test = new StateSteps(stateful);
        test.startCheck(StateSteps.definitely(JobState.READY),
                StateSteps.maybe(JobState.EXECUTING),
                StateSteps.definitely(JobState.INCOMPLETE));

        stateful.advance();

        try {
            test.checkNow();
            fail("Should fail");
        }
        catch (IllegalStateException e) {
            assertThat(e.getMessage(),
                    is("Not enough states for [SomeStateful]: expected [Definitely READY, Maybe EXECUTING, Definitely INCOMPLETE], but was only [READY, EXECUTING]."));
        }
    }

    @Test
    public void whenMaybeThenWrongMessageIsOk() {

        SomeStateful stateful = new SomeStateful(JobState.READY,
                JobState.EXECUTING, JobState.COMPLETE);

        StateSteps test = new StateSteps(stateful);
        test.startCheck(StateSteps.definitely(JobState.READY),
                StateSteps.maybe(JobState.EXECUTING),
                StateSteps.definitely(JobState.INCOMPLETE));

        stateful.advance();
        stateful.advance();

        try {
            test.checkNow();
            fail("Should fail");
        }
        catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Expected INCOMPLETE, but was COMPLETE (index 2)"));
        }
    }
}