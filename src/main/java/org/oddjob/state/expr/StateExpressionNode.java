package org.oddjob.state.expr;

import java.text.ParseException;
import java.util.Optional;
import java.util.function.Consumer;

import org.oddjob.arooa.deploy.annotations.ArooaText;
import org.oddjob.events.EventSourceBase;
import org.oddjob.events.EventOf;
import org.oddjob.events.Trigger;
import org.oddjob.util.Restore;

/**
 * @oddjob.description Evaluate a state expression that becomes an event source for triggering other jobs.
 *
 * @see Trigger
 * 
 * @author rob
 *
 */
public class StateExpressionNode extends EventSourceBase<EventOf<Boolean>> {

	private String expression;

	@Override
	protected Restore doStart(Consumer<? super EventOf<Boolean>> consumer) throws ParseException {

		String nonNullExpr = Optional.ofNullable(this.expression)
				.orElseThrow(() -> new IllegalStateException("No expression"));

			StateExpressionParser<StateExpression> expressionParser = new StateExpressionParser<>(
					CaptureToExpression::new);
			
			StateExpression expression = expressionParser.parse(nonNullExpr);

			return expression.evaluate(getArooaSession(), 
					v -> v.onSuccess( 
							b -> {
								if (b.getOf()) {
									consumer.accept(b);
								}
							})
					.onFailure(this::setStateException));
	}
	

	public String getExpression() {
		return expression;
	}

	@ArooaText
	public void setExpression(String expression) {
		this.expression = expression;
	}
		
}
