package org.oddjob;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.oddjob.state.State;
import org.oddjob.state.StateCondition;
import org.oddjob.state.StateEvent;

public class OddjobMatchers {

    public static Matcher<State> stateIs(StateCondition stateCondition) {
        return new StateMatcher(stateCondition);
    }

    public static Matcher<StateEvent> stateEventIs(StateCondition stateCondition) {
        return new StateEventMatcher(stateCondition);
    }

    public static Matcher<Stateful> statefulIs(StateCondition stateCondition) {
        return new StatefulMatcher(stateCondition);
    }

    static class StateMatcher extends BaseMatcher<State> {

        private final StateCondition toMatch;

        StateMatcher(StateCondition toMatch) {
            this.toMatch = toMatch;
        }

        @Override
        public boolean matches(Object item) {
            if (item instanceof State) {
                return toMatch.test((State) item);
            }
            else {
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("State " + toMatch + " ");
        }
    }

    static class StateEventMatcher extends BaseMatcher<StateEvent> {

        private final StateCondition toMatch;

        StateEventMatcher(StateCondition toMatch) {
            this.toMatch = toMatch;
        }

        @Override
        public boolean matches(Object item) {
            if (item instanceof StateEvent) {
                return toMatch.test((StateEvent) item);
            }
            else {
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("State " + toMatch + " ");
        }
    }

    static class StatefulMatcher extends BaseMatcher<Stateful> {

        private final StateCondition toMatch;

        StatefulMatcher(StateCondition toMatch) {
            this.toMatch = toMatch;
        }

        @Override
        public boolean matches(Object item) {
            if (item instanceof Stateful) {
                return toMatch.test(((Stateful) item).lastStateEvent().getState());
            }
            else {
                return false;
            }
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            if (item instanceof Stateful) {
                description.appendText("State of [" + item + "] was " + ((Stateful) item).lastStateEvent().getState());
            }
            else {
                description.appendText("[" + item + "] not Stateful");
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("State " + toMatch);
        }
    }
}
