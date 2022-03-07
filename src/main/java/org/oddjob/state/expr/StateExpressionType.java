package org.oddjob.state.expr;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.deploy.annotations.ArooaText;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.events.InstantEvent;
import org.oddjob.events.InstantEventSource;
import org.oddjob.util.Restore;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @oddjob.description Evaluate a state expression that becomes an event source for triggering other jobs.
 * used with {@link org.oddjob.events.Trigger} and {@link org.oddjob.events.When}.
 *
 * @oddjob.example
 *
 * Runs a job when two other jobs complete, but only if one of the jobs hasn't been run
 *
 * {@oddjob.xml.resource org/oddjob/state/expr/StateExpressionTimeExample.xml}
 *
 * @author rob
 *
 */
public class StateExpressionType implements InstantEventSource<Boolean>, ArooaSessionAware {

	private String expression;

	private ArooaSession session;

	@ArooaHidden
	@Override
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}

	@Override
	public Restore subscribe(Consumer<? super InstantEvent<Boolean>> consumer) throws Exception {

		String nonNullExpr = Optional.ofNullable(this.expression)
				.orElseThrow(() -> new IllegalStateException("No expression"));

			StateExpressionParser<StateExpression> expressionParser = new StateExpressionParser<>(
					CaptureToExpression::new);
			
			StateExpression expression = expressionParser.parse(nonNullExpr);

			return expression.evaluate(session,
					v -> v.onSuccess( 
							b -> {
								if (b.getOf()) {
									consumer.accept(b);
								}
							})
					.orElseThrow());
	}
	

	public String getExpression() {
		return expression;
	}

	@ArooaText
	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return "StateExpression: '" + expression + '\'';
	}
}
