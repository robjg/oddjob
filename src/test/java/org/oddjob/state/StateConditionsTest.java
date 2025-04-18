package org.oddjob.state;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StateConditionsTest {

    static class Tester<T extends Enum<T> & State> {

        private final StateCondition test;

        private final Set<T> states;

        private final List<Runnable> tests = new ArrayList<>();

        Tester(StateCondition test, Class<T> stateEnum) {
            this.test = test;
            this.states = new HashSet<>(EnumSet.allOf(stateEnum));
            this.states.remove(Enum.valueOf(stateEnum, "DESTROYED"));
        }

        void remove(T state) {
            if (!states.remove(state)) {
                throw new IllegalArgumentException(state + " not in expected states.");
            }
        }

        Tester<T> expectFalse(T state) {
            remove(state);
            tests.add(() -> assertThat("Expected " + test + " condition of state " + state + " to be false ",
                    test.test(state), is(false)));
            return this;
        }

        Tester<T> expectTrue(T state) {
            remove(state);
            tests.add(() -> assertThat("Expected " + test + " condition of state " + state + " to be true ",
                    test.test(state), is(true)));
            return this;
        }

        void test() {
            if (!states.isEmpty()) {
                throw new IllegalStateException("No assertion for " + states);
            }
            tests.forEach(Runnable::run);
        }

    }

    static <T extends Enum<T> & State> Tester<T> testOf(StateCondition test, Class<T> stateEnum) {

        return new Tester<>(test, stateEnum);
    }

    @Test
    public void testCompleteAllStates() {

        testOf(StateConditions.COMPLETE, JobState.class)
                .expectFalse(JobState.READY)
                .expectFalse(JobState.EXECUTING)
                .expectFalse(JobState.ACTIVE)
                .expectTrue(JobState.COMPLETE)
                .expectFalse(JobState.INCOMPLETE)
                .expectFalse(JobState.EXCEPTION)
                .test();

        testOf(StateConditions.COMPLETE, ServiceState.class)
                .expectFalse(ServiceState.STARTABLE)
                .expectFalse(ServiceState.STARTING)
                .expectFalse(ServiceState.INITIALISING)
                .expectTrue(ServiceState.STARTED)
                .expectFalse(ServiceState.EXCEPTION)
                .expectTrue(ServiceState.STOPPED)
                .test();

        testOf(StateConditions.COMPLETE, ParentState.class)
                .expectFalse(ParentState.READY)
                .expectFalse(ParentState.EXECUTING)
                .expectFalse(ParentState.ACTIVE)
                .expectTrue(ParentState.STARTED)
                .expectTrue(ParentState.COMPLETE)
                .expectFalse(ParentState.INCOMPLETE)
                .expectFalse(ParentState.EXCEPTION)
                .test();
    }

    @Test
    public void testDoneAllStates() {

        testOf(StateConditions.DONE, JobState.class)
                .expectFalse(JobState.READY)
                .expectFalse(JobState.EXECUTING)
                .expectFalse(JobState.ACTIVE)
                .expectTrue(JobState.COMPLETE)
                .expectFalse(JobState.INCOMPLETE)
                .expectFalse(JobState.EXCEPTION)
                .test();

        testOf(StateConditions.DONE, ServiceState.class)
                .expectFalse(ServiceState.STARTABLE)
                .expectFalse(ServiceState.INITIALISING)
                .expectFalse(ServiceState.STARTING)
                .expectFalse(ServiceState.STARTED)
                .expectFalse(ServiceState.EXCEPTION)
                .expectTrue(ServiceState.STOPPED)
                .test();

        testOf(StateConditions.DONE, ParentState.class)
                .expectFalse(ParentState.READY)
                .expectFalse(ParentState.EXECUTING)
                .expectFalse(ParentState.ACTIVE)
                .expectFalse(ParentState.STARTED)
                .expectTrue(ParentState.COMPLETE)
                .expectFalse(ParentState.INCOMPLETE)
                .expectFalse(ParentState.EXCEPTION)
                .test();
    }

    @Test
    public void testFinishedAllStates() {

        testOf(StateConditions.FINISHED, JobState.class)
                .expectFalse(JobState.READY)
                .expectFalse(JobState.EXECUTING)
                .expectFalse(JobState.ACTIVE)
                .expectTrue(JobState.COMPLETE)
                .expectTrue(JobState.INCOMPLETE)
                .expectTrue(JobState.EXCEPTION)
                .test();

        testOf(StateConditions.FINISHED, ServiceState.class)
                .expectFalse(ServiceState.STARTABLE)
                .expectFalse(ServiceState.INITIALISING)
                .expectFalse(ServiceState.STARTING)
                .expectTrue(ServiceState.STARTED)
                .expectTrue(ServiceState.EXCEPTION)
                .expectTrue(ServiceState.STOPPED)
                .test();

        testOf(StateConditions.FINISHED, ParentState.class)
                .expectFalse(ParentState.READY)
                .expectFalse(ParentState.EXECUTING)
                .expectFalse(ParentState.ACTIVE)
                .expectTrue(ParentState.STARTED)
                .expectTrue(ParentState.COMPLETE)
                .expectTrue(ParentState.INCOMPLETE)
                .expectTrue(ParentState.EXCEPTION)
                .test();
    }

    @Test
    public void testEndedAllStates() {

        testOf(StateConditions.ENDED, JobState.class)
                .expectFalse(JobState.READY)
                .expectFalse(JobState.EXECUTING)
                .expectFalse(JobState.ACTIVE)
                .expectTrue(JobState.COMPLETE)
                .expectTrue(JobState.INCOMPLETE)
                .expectTrue(JobState.EXCEPTION)
                .test();

        testOf(StateConditions.ENDED, ServiceState.class)
                .expectFalse(ServiceState.STARTABLE)
                .expectFalse(ServiceState.INITIALISING)
                .expectFalse(ServiceState.STARTING)
                .expectFalse(ServiceState.STARTED)
                .expectTrue(ServiceState.EXCEPTION)
                .expectTrue(ServiceState.STOPPED)
                .test();

        testOf(StateConditions.ENDED, ParentState.class)
                .expectFalse(ParentState.READY)
                .expectFalse(ParentState.EXECUTING)
                .expectFalse(ParentState.ACTIVE)
                .expectFalse(ParentState.STARTED)
                .expectTrue(ParentState.COMPLETE)
                .expectTrue(ParentState.INCOMPLETE)
                .expectTrue(ParentState.EXCEPTION)
                .test();
    }

    @Test
    public void testFailureAllStates() {

        testOf(StateConditions.FAILURE, JobState.class)
                .expectFalse(JobState.READY)
                .expectFalse(JobState.EXECUTING)
                .expectFalse(JobState.ACTIVE)
                .expectFalse(JobState.COMPLETE)
                .expectTrue(JobState.INCOMPLETE)
                .expectTrue(JobState.EXCEPTION)
                .test();

        testOf(StateConditions.FAILURE, ServiceState.class)
                .expectFalse(ServiceState.STARTABLE)
                .expectFalse(ServiceState.INITIALISING)
                .expectFalse(ServiceState.STARTING)
                .expectFalse(ServiceState.STARTED)
                .expectTrue(ServiceState.EXCEPTION)
                .expectFalse(ServiceState.STOPPED)
                .test();

        testOf(StateConditions.FAILURE, ParentState.class)
                .expectFalse(ParentState.READY)
                .expectFalse(ParentState.EXECUTING)
                .expectFalse(ParentState.ACTIVE)
                .expectFalse(ParentState.STARTED)
                .expectFalse(ParentState.COMPLETE)
                .expectTrue(ParentState.INCOMPLETE)
                .expectTrue(ParentState.EXCEPTION)
                .test();
    }

    @Test
    public void testRunningAllStates() {

        testOf(StateConditions.RUNNING, JobState.class)
                .expectFalse(JobState.READY)
                .expectTrue(JobState.EXECUTING)
                .expectTrue(JobState.ACTIVE)
                .expectFalse(JobState.COMPLETE)
                .expectFalse(JobState.INCOMPLETE)
                .expectFalse(JobState.EXCEPTION)
                .test();

        testOf(StateConditions.RUNNING, ServiceState.class)
                .expectFalse(ServiceState.STARTABLE)
                .expectTrue(ServiceState.INITIALISING)
                .expectTrue(ServiceState.STARTING)
                .expectTrue(ServiceState.STARTED)
                .expectFalse(ServiceState.EXCEPTION)
                .expectFalse(ServiceState.STOPPED)
                .test();

        testOf(StateConditions.RUNNING, ParentState.class)
                .expectFalse(ParentState.READY)
                .expectTrue(ParentState.EXECUTING)
                .expectTrue(ParentState.ACTIVE)
                .expectTrue(ParentState.STARTED)
                .expectFalse(ParentState.COMPLETE)
                .expectFalse(ParentState.INCOMPLETE)
                .expectFalse(ParentState.EXCEPTION)
                .test();
    }

    @Test
    public void testStartedAllStates() {

        testOf(StateConditions.STARTED, JobState.class)
                .expectFalse(JobState.READY)
                .expectFalse(JobState.EXECUTING)
                .expectFalse(JobState.ACTIVE)
                .expectFalse(JobState.COMPLETE)
                .expectFalse(JobState.INCOMPLETE)
                .expectFalse(JobState.EXCEPTION)
                .test();

        testOf(StateConditions.STARTED, ServiceState.class)
                .expectFalse(ServiceState.STARTABLE)
                .expectFalse(ServiceState.INITIALISING)
                .expectFalse(ServiceState.STARTING)
                .expectTrue(ServiceState.STARTED)
                .expectFalse(ServiceState.EXCEPTION)
                .expectFalse(ServiceState.STOPPED)
                .test();

        testOf(StateConditions.STARTED, ParentState.class)
                .expectFalse(ParentState.READY)
                .expectFalse(ParentState.EXECUTING)
                .expectFalse(ParentState.ACTIVE)
                .expectTrue(ParentState.STARTED)
                .expectFalse(ParentState.COMPLETE)
                .expectFalse(ParentState.INCOMPLETE)
                .expectFalse(ParentState.EXCEPTION)
                .test();
    }

    @Test
    public void testActiveAllStates() {

        testOf(StateConditions.ACTIVE, JobState.class)
                .expectFalse(JobState.READY)
                .expectFalse(JobState.EXECUTING)
                .expectTrue(JobState.ACTIVE)
                .expectFalse(JobState.COMPLETE)
                .expectFalse(JobState.INCOMPLETE)
                .expectFalse(JobState.EXCEPTION)
                .test();

        testOf(StateConditions.ACTIVE, ServiceState.class)
                .expectFalse(ServiceState.STARTABLE)
                .expectTrue(ServiceState.INITIALISING)
                .expectFalse(ServiceState.STARTING)
                .expectFalse(ServiceState.STARTED)
                .expectFalse(ServiceState.EXCEPTION)
                .expectFalse(ServiceState.STOPPED)
                .test();

        testOf(StateConditions.ACTIVE, ParentState.class)
                .expectFalse(ParentState.READY)
                .expectFalse(ParentState.EXECUTING)
                .expectTrue(ParentState.ACTIVE)
                .expectFalse(ParentState.STARTED)
                .expectFalse(ParentState.COMPLETE)
                .expectFalse(ParentState.INCOMPLETE)
                .expectFalse(ParentState.EXCEPTION)
                .test();
    }

    @Test
    public void testLiveAllStates() {

        testOf(StateConditions.LIVE, JobState.class)
                .expectFalse(JobState.READY)
                .expectFalse(JobState.EXECUTING)
                .expectTrue(JobState.ACTIVE)
                .expectFalse(JobState.COMPLETE)
                .expectFalse(JobState.INCOMPLETE)
                .expectFalse(JobState.EXCEPTION)
                .test();

        testOf(StateConditions.LIVE, ServiceState.class)
                .expectFalse(ServiceState.STARTABLE)
                .expectTrue(ServiceState.INITIALISING)
                .expectFalse(ServiceState.STARTING)
                .expectTrue(ServiceState.STARTED)
                .expectFalse(ServiceState.EXCEPTION)
                .expectFalse(ServiceState.STOPPED)
                .test();

        testOf(StateConditions.LIVE, ParentState.class)
                .expectFalse(ParentState.READY)
                .expectFalse(ParentState.EXECUTING)
                .expectTrue(ParentState.ACTIVE)
                .expectTrue(ParentState.STARTED)
                .expectFalse(ParentState.COMPLETE)
                .expectFalse(ParentState.INCOMPLETE)
                .expectFalse(ParentState.EXCEPTION)
                .test();
    }
}
