package org.oddjob.tools;

import org.oddjob.Stateful;
import org.oddjob.arooa.utils.ClassUtils;
import org.oddjob.state.State;
import org.oddjob.state.StateCondition;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Test Utility class to track state changes.
 *
 * @author rob
 */
public class StateSteps {
    private static final Logger logger = LoggerFactory.getLogger(StateSteps.class);

    private final Stateful stateful;

    private volatile Check listener;

    private volatile long timeout = 5000L;

    public StateSteps(Stateful stateful) {
        if (stateful == null) {
            throw new NullPointerException("No Stateful.");
        }
        this.stateful = stateful;
    }

    public static CheckConstruct definitely(State ready) {
        return new DefinitelyCheck(ready);
    }

    public static CheckConstruct maybe(State ready) {
        return new MaybeCheck(ready);
    }

    interface Check extends StateListener {

        boolean isDone();

        String getFailureMessage();

        boolean isFailure();

        void failNow();
    }


    public interface CheckConstruct {
        void addTo(ComplexCheck builder);
    }

    static class DefinitelyCheck implements CheckConstruct {

        private final State state;

        DefinitelyCheck(State state) {

            this.state = state;
        }

        @Override
        public void addTo(ComplexCheck builder) {
            builder.addDefinitely(state);
        }
    }

    static class MaybeCheck implements CheckConstruct {

        private final State state;

        MaybeCheck(State state) {

            this.state = state;
        }

        @Override
        public void addTo(ComplexCheck builder) {
            builder.addMaybe(state);
        }
    }

    class ComplexCheck implements Check {

        private volatile int index;

        private volatile String failureMessage;

        private volatile boolean done;

        private final List<Predicate<State>> predicates = new ArrayList<>();

        private final List<State> received = new ArrayList<>();

        public void addMaybe(State state) {
            final int us = predicates.size();
            predicates.add(new Predicate<State>() {
                @Override
                public boolean test(State now) {
                    if (now == state) {
                        return true;
                    }
                    if (us == predicates.size() - 1) {
                        return true;
                    }
                    return predicates.get(us + 1).test(now);
                }

                @Override
                public String toString() {
                    return "Maybe " + state;
                }
            });
        }

        public void addDefinitely(State state) {
            final int us = predicates.size();
            predicates.add(new Predicate<State>() {
                @Override
                public boolean test(State now) {
                    if (now == state) {
                        index = us + 1;
                        return true;
                    } else {
                        failureMessage =
                                "Expected " + state +
                                        ", but was " + now +
                                        " (index " + us + ")";
                        return false;
                    }
                }

                @Override
                public String toString() {
                    return "Definitely " + state;
                }
            });

        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public String getFailureMessage() {
            return failureMessage;
        }

        @Override
        public boolean isFailure() {
            return failureMessage != null;
        }

        @Override
        public void failNow() {
            throw new IllegalStateException(
                    "Not enough states for [" + stateful + "]: expected " +
                            predicates +
                            ", but was only " + received + ".");
        }

        @Override
        public void jobStateChange(StateEvent event) {
            String position;
            if (failureMessage != null) {
                position = "(failure pending)";
            } else {
                position = "for index [" + index + "]";
            }

            logger.info("Received [" + event.getState() +
                    "] " + position + " from [" + event.getSource() + "]");

            if (failureMessage != null) {
                return;
            }

            if (index >= predicates.size()) {
                failureMessage =
                        "More states than expected: " + event.getState() +
                                " (index " + index + ")";
            } else {
                synchronized (StateSteps.this) {
                    received.add(event.getState());

                    if (predicates.get(index).test(event.getState())) {
                        if (index == predicates.size()) {
                            done = true;
                            StateSteps.this.notifyAll();
                        }
                    } else {
                        done = true;
                        StateSteps.this.notifyAll();
                    }
                }
            }
        }

        @Override
        public String toString() {
            return "State Check [" + stateful + "] to have states " +
                    predicates;
        }
    }

    interface SingleCheck {

        Optional<String> test(State incoming);
    }

    static class SingleStateCheck implements SingleCheck {

        private final State expected;

        SingleStateCheck(State expected) {
            this.expected = expected;
        }

        @Override
        public Optional<String> test(State incoming) {
            if (expected.equals(incoming)) {
                return Optional.empty();
            }
            else {
                return Optional.of(
                        "Expected " + expected + "(" + ClassUtils.getSimpleName(expected.getClass()) + ")" +
                        ", was " + incoming + "(" + ClassUtils.getSimpleName(incoming.getClass()) + ")");
            }
        }
    }

    static class SingleConditionCheck implements SingleCheck {

        private final StateCondition expected;

        SingleConditionCheck(StateCondition expected) {
            this.expected = expected;
        }

        @Override
        public Optional<String> test(State incoming) {
            if (expected.test(incoming)) {
                return Optional.empty();
            }
            else {
                return Optional.of(
                        "Expected Condition " + expected +
                                ", was " + incoming + "(" + ClassUtils.getSimpleName(incoming.getClass()) + ")"
                );
            }
        }
    }

    class Listener implements Check {

        private final SingleCheck[] steps;

        private volatile int index;

        private volatile boolean done;

        private volatile String failureMessage;

        Listener(State[] steps) {
            this.steps = Arrays.stream(steps)
                    .map(SingleStateCheck::new)
                    .toArray(SingleCheck[]::new);
        }

        Listener(StateCondition[] steps) {
            this.steps = Arrays.stream(steps)
                    .map(SingleConditionCheck::new)
                    .toArray(SingleCheck[]::new);
		}
        
        @Override
        public void jobStateChange(StateEvent event) {
            String position;
            if (failureMessage != null) {
                position = "(failure pending)";
            } else {
                position = "for index [" + index + "]";
            }

            logger.info("Received [" + event.getState() +
                    "] " + position + " from [" + event.getSource() + "]");

            if (failureMessage != null) {
                return;
            }

            if (index >= steps.length) {
                failureMessage =
                        "More states than expected: " + event.getState() +
                                " (index " + index + ")";
            } else {
                synchronized (StateSteps.this) {
                    SingleCheck expected = steps[index];
                    State incoming = event.getState();
                    failureMessage = expected.test(incoming)
                            .map(msg -> msg + " (index " + index + ")")
                            .orElse(null);

                    if (failureMessage == null) {
                        if (++index == steps.length) {
                            done = true;
                            StateSteps.this.notifyAll();
                        }
                    } else {
                        done = true;
                        StateSteps.this.notifyAll();
                    }
                }
            }
        }

        public synchronized boolean isDone() {
            return done;
        }

        @Override
        public String getFailureMessage() {
            return failureMessage;
        }

        @Override
        public boolean isFailure() {
            return failureMessage != null;
        }

        @Override
        public void failNow() {
            throw new IllegalStateException(
                    "Not enough states for [" + stateful + "]: expected " +
                            steps.length + " " +
                            Arrays.toString(steps) +
                            ", was only first " + index + ".");
        }

        @Override
        public String toString() {
            return "State Check [" + stateful + "] to have states " +
                    Arrays.toString(steps);
        }
    }

    public synchronized void startCheck(CheckConstruct... constructs) {

        ComplexCheck complexCheck = new ComplexCheck();
        for (CheckConstruct construct : constructs) {
            construct.addTo(complexCheck);
        }
        startCheck(complexCheck);
    }

    public void startCheck(State... steps) {

        if (steps == null || steps.length == 0) {
            throw new IllegalStateException("No steps!");
        }

        startCheck(new Listener(steps));
    }

	public void startCheck(StateCondition... steps) {

		if (steps == null || steps.length == 0) {
			throw new IllegalStateException("No steps!");
		}

		startCheck(new Listener(steps));
	}

	public synchronized void startCheck(Check check) {

        if (listener != null) {
            throw new IllegalStateException("Check in progress!");
        }

        logger.info("Starting check on " + check);

        this.listener = check;

        stateful.addStateListener(listener);
    }

    public void checkNow() {

        try {
            if (listener.isDone()) {
                if (listener.isFailure()) {
                    throw new IllegalStateException(listener.getFailureMessage());
                }
            } else {
                listener.failNow();
            }
        } catch (IllegalStateException e) {
            logger.error("Failed", e);
            throw e;
        } finally {
            stateful.removeStateListener(listener);
            listener = null;
        }
    }

    public void checkWait() throws InterruptedException {
        if (listener == null) {
            throw new IllegalStateException("No Check In Progress.");
        }

        logger.info("Waiting" +
                " on " + listener);

        if (!listener.isDone()) {

            synchronized (this) {
                wait(timeout);
            }

            logger.info("Woken or Timedout " +
                    " on " + listener);
        }

        checkNow();

        logger.info("Waiting complete on [" + stateful + "]");
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

}
