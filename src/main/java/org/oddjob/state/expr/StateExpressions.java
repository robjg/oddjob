package org.oddjob.state.expr;

import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.utils.Try;
import org.oddjob.events.BinaryEvaluation;
import org.oddjob.events.EventOf;
import org.oddjob.events.UnaryEvaluation;
import org.oddjob.state.StateCondition;
import org.oddjob.state.StateListener;
import org.oddjob.util.Restore;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

/**
 * Implementations of {@link StateExpression}s.
 *
 * @author rob
 * @see CaptureToExpression
 */
public class StateExpressions {

    public static class Equals implements StateExpression {

        private final String jobId;

        private final StateCondition stateCondition;

        public Equals(String jobId, StateCondition stateCondition) {
            this.jobId = jobId;
            this.stateCondition = stateCondition;
        }

        @Override
        public Restore evaluate(ArooaSession session,
                                Consumer<? super Try<EventOf<Boolean>>> results) {

            Stateful stateful;
            try {
                stateful = session.getBeanRegistry().lookup(jobId, Stateful.class);
            } catch (ArooaPropertyException | ArooaConversionException e) {
                results.accept(Try.fail(e));
                return Restore.nothing();
            }

            if (stateful == null) {
                results.accept(Try.fail(new NullPointerException("No " + jobId)));
                return Restore.nothing();
            }

            StateListener listener = event -> results.accept(Try.of(
                    new StateEvaluation(stateCondition.test(event.getState()),
                                        event)));

            stateful.addStateListener(listener);

            return () -> stateful.removeStateListener(listener);
        }
    }

    public static class Or extends BinaryLogic {

        public Or(StateExpression lhs, StateExpression rhs) {
            super(lhs, rhs,
                  (l, r) ->
                          new BinaryEvaluation<>(
                                  l.getOf() || r.getOf(),
                                  l,
                                  r));
        }
    }

    public static class And extends BinaryLogic {

        public And(StateExpression lhs, StateExpression rhs) {
            super(lhs, rhs, (l, r) ->
                    new BinaryEvaluation<>(
                            l.getOf() && r.getOf(),
                            l,
                            r));
        }
    }

    public static class BinaryLogic implements StateExpression {

        private final StateExpression lhs;

        private final StateExpression rhs;

        private final BinaryOperator<EventOf<Boolean>> logic;

        public BinaryLogic(StateExpression lhs, StateExpression rhs,
                           BinaryOperator<EventOf<Boolean>> logic) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.logic = logic;
        }

        @Override
        public Restore evaluate(ArooaSession session,
                                Consumer<? super Try<EventOf<Boolean>>> results) {

            AtomicReference<Try<EventOf<Boolean>>> lResult =
                    new AtomicReference<>();
            AtomicReference<Try<EventOf<Boolean>>> rResult =
                    new AtomicReference<>();

            class Evaluator implements Consumer<Try<EventOf<Boolean>>> {

                private final AtomicReference<Try<EventOf<Boolean>>> ours;
                private final AtomicReference<Try<EventOf<Boolean>>> other;

                Evaluator(AtomicReference<Try<EventOf<Boolean>>> ours,
                          AtomicReference<Try<EventOf<Boolean>>> other) {
                    this.ours = ours;
                    this.other = other;
                }

                @Override
                public void accept(Try<EventOf<Boolean>> t) {
                    ours.set(t);

                    Try<EventOf<Boolean>> otherResult = other.get();
                    if (otherResult == null) {
                        return;
                    }

                    Try<EventOf<Boolean>> ourResult =
                            t.flatMap(ourBool ->
                                              otherResult.map(otherBool ->
                                                                      logic.apply(ourBool, otherBool)));

                    results.accept(ourResult);
                }
            }

            Restore restoreL = lhs.evaluate(session, new Evaluator(lResult, rResult));

            Restore restoreR = rhs.evaluate(session, new Evaluator(rResult, lResult));

            return () -> {
                restoreL.close();
                restoreR.close();
            };
        }
    }

    public static class Not implements StateExpression {

        private final StateExpression expr;

        public Not(StateExpression expr) {
            this.expr = expr;
        }

        @Override
        public Restore evaluate(ArooaSession session,
                                Consumer<? super Try<EventOf<Boolean>>> results) {

            return expr.evaluate(
                    session,
                    r -> results.accept(
                            r.map(booleanEvent ->
                                          new UnaryEvaluation<>(
                                                  !booleanEvent.getOf(),
                                                  booleanEvent))));
        }
    }
}
