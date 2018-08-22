package org.oddjob.state.expr;

import java.text.ParseException;
import java.util.Optional;
import java.util.function.Consumer;

import org.oddjob.arooa.deploy.annotations.ArooaText;
import org.oddjob.events.EventSourceBase;
import org.oddjob.events.Trigger;
import org.oddjob.util.Restore;

/**
 * Provide a State Expression.
 * 
 * @see Trigger
 * 
 * @author rob
 *
 */
public class StateExpressionNode extends EventSourceBase<Boolean> {

	private String expression;
	
	
	@Override
	protected Restore doStart(Consumer<? super Boolean> consumer) throws ParseException {

		String nonNullExpr = Optional.ofNullable(this.expression)
				.orElseThrow(() -> new IllegalStateException("No expression"));

			StateExpressionParser<StateExpression> expressionParser = new StateExpressionParser<>(
				() -> new CaptureToExpression());		
			
			StateExpression expression = expressionParser.parse(nonNullExpr);

			return expression.evaluate(getArooaSession(), 
					v -> v.onSuccess( 
							b -> {
								if (b) {
									consumer.accept(b);
								}
							})
					.onFailure( e -> setStateException(e)));
	}
	

	public String getExpression() {
		return expression;
	}

	@ArooaText
	public void setExpression(String expression) {
		this.expression = expression;
	}
		
}
