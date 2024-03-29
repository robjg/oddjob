package org.oddjob.state.expr;

import org.oddjob.arooa.deploy.annotations.ArooaText;
import org.oddjob.arooa.utils.Try;
import org.oddjob.events.InstantEvent;
import org.oddjob.framework.extend.SimpleJob;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @oddjob.description Evaluate a state expression and become COMPLETE if it is true or INCOMPLETE otherwise.
 * <p>
 *     Can be useful with an {@link org.oddjob.state.IfJob}. The expression is the same as that described
 *     in {@link StateExpressionType}.
 * </p>
 *
 */
public class StateExpressionJob extends SimpleJob {

    /**
     * @oddjob.property
     * @oddjob.description The expression.
     * @oddjob.required Yes.
     */
    private String expression;

    /**
     * @oddjob.property
     * @oddjob.description The event that is the result of the evaluation.
     * @oddjob.required Read only.
     */
    private InstantEvent<Boolean> evaluation;


    @Override
    protected int execute() throws Throwable {

        String nonNullExpr =
                Optional.ofNullable(this.expression)
                        .orElseThrow(() -> new IllegalStateException(
                                "No expression"));

        StateExpressionParser<StateExpression> expressionParser = new StateExpressionParser<>(
                CaptureToExpression::new);

        StateExpression expression = expressionParser.parse(nonNullExpr);

        AtomicReference<Try<InstantEvent<Boolean>>> result =
                new AtomicReference<>();
        expression.evaluate(getArooaSession(), result::set);

        this.evaluation = result.get().orElseThrow();


        return this.evaluation.getOf() ? 0 : 1;
    }

    public String getExpression() {
        return expression;
    }

    @ArooaText
    public void setExpression(String expression) {
        this.expression = expression;
    }

    public InstantEvent<Boolean> getEvaluation() {
        return evaluation;
    }
}
