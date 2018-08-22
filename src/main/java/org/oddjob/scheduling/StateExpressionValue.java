package org.oddjob.scheduling;

import java.text.ParseException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaText;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.arooa.utils.Try;
import org.oddjob.state.expr.CaptureToExpression;
import org.oddjob.state.expr.StateExpression;
import org.oddjob.state.expr.StateExpressionParser;
import org.oddjob.util.Restore;

/**
 * Provide a State Expression.
 * 
 * @see Trigger2
 * 
 * @author rob
 *
 */
public class StateExpressionValue 
implements ValueFactory<Function<Consumer<Try<Boolean>>, Restore >>,
		ArooaSessionAware {

	private ArooaSession arooaSession;
	
	private String expression;
	
	@Override
	public void setArooaSession(ArooaSession session) {
		this.arooaSession = session;
	}	
	
	@Override
	public Function<Consumer<Try<Boolean>>, Restore> toValue() throws ArooaConversionException {

		String nonNullExpr = Optional.ofNullable(this.expression)
			.orElseThrow(() -> new IllegalStateException("No expression"));

		StateExpressionParser<StateExpression> expressionParser = new StateExpressionParser<>(
			() -> new CaptureToExpression());		
		
		StateExpression expression;
		try {
			expression = expressionParser.parse(nonNullExpr);
		} catch (ParseException e) {
			throw new ArooaConversionException(e);
		}

		return new Function<Consumer<Try<Boolean>>, Restore>() {
		
			@Override
			public Restore apply(Consumer<Try<Boolean>> consumer) {			
				return expression.evaluate(arooaSession, consumer);
			}
			
			@Override
			public String toString() {		
				return nonNullExpr;
			}
		};
	}

	public String getExpression() {
		return expression;
	}

	@ArooaText
	public void setExpression(String expression) {
		this.expression = expression;
	}
		
}
