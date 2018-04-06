package org.oddjob.state.expr;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.utils.Try;
import org.oddjob.state.StateCondition;
import org.oddjob.state.StateListener;
import org.oddjob.util.Restore;

/**
 * Implementations of {@link StateExpression}s. 
 * 
 * @see CaptureToExpression
 * 
 * @author rob
 *
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
		public Restore evaluate(ArooaSession session, Consumer<Try<Boolean>> results) {
			
			Stateful stateful;
			try {
				stateful = session.getBeanRegistry().lookup(jobId, Stateful.class);
			} catch (ArooaPropertyException | ArooaConversionException e) {
				results.accept(Try.fail(e));
				return Restore.nothing() ;
			}
			if (stateful == null) {
				results.accept(Try.fail(new NullPointerException("No " + jobId)));
				return Restore.nothing();
			}
			StateListener l = e -> results.accept(Try.of(stateCondition.test(e.getState())));
			stateful.addStateListener(l);
						
			return () -> stateful.removeStateListener(l);
		}
	}
	
	public static class Or extends BinaryLogic {
		
		public Or(StateExpression lhs, StateExpression rhs) {
			super(lhs, rhs, (l, r) -> l || r);
		}
		
	}
	
	public static class And extends BinaryLogic {
		
		public And(StateExpression lhs, StateExpression rhs) {
			super(lhs, rhs, (l, r) -> l && r);
		}
	}

	public static class BinaryLogic implements StateExpression {
		
		private final StateExpression lhs;
		
		private final StateExpression rhs;

		private final BinaryOperator<Boolean> logic;
		
		public BinaryLogic(StateExpression lhs, StateExpression rhs, BinaryOperator<Boolean> logic) {
			this.lhs = lhs;
			this.rhs = rhs;
			this.logic = logic;
		}

		@Override
		public Restore evaluate(ArooaSession session, Consumer<Try<Boolean>> results) {
			
			AtomicReference<Try<Boolean>> lResult = new AtomicReference<>();
			AtomicReference<Try<Boolean>> rResult = new AtomicReference<>();			
			
			class Evaluator implements Consumer<Try<Boolean>> {
				
				private final AtomicReference<Try<Boolean>> ours;
				private final AtomicReference<Try<Boolean>> other;

				Evaluator(AtomicReference<Try<Boolean>> ours, AtomicReference<Try<Boolean>> other) {
					this.ours = ours;
					this.other = other;
				}
				
				@Override
				public void accept(Try<Boolean> t) {					
					ours.set(t);
					
					Try<Boolean> otherResult = other.get();
					if (otherResult == null) {
						return;
					}
					
					Try<Boolean> ourResult = t.flatMap( ourBool -> 
							otherResult.map(otherBool -> logic.apply(ourBool, otherBool)));
					
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
		public Restore evaluate(ArooaSession session, Consumer<Try<Boolean>> results) {
			
			return expr.evaluate(session, 
					r -> results.accept(r.map(b -> !b)));
		}		
	}
}
