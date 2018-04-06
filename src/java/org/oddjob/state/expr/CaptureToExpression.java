package org.oddjob.state.expr;

import java.util.Deque;
import java.util.LinkedList;

import org.oddjob.state.StateCondition;
import org.oddjob.state.StateConditions;

/**
 * A {@link StateExpressionCapture} that creates a {@link StateExpression}.
 * 
 * @author rob
 *
 */
public class CaptureToExpression implements StateExpressionCapture<StateExpression> {

	private final Deque<StateExpression> stack = new LinkedList<>();
	
	@Override
	public void is(String job, String state) {
		
		StateCondition stateCondition = StateConditions.valueOf(state.toUpperCase());
		stack.push(new StateExpressions.Equals(job, stateCondition));
	}
	
	@Override
	public void not() {
		
		StateExpression last = stack.pop();
		stack.push(new StateExpressions.Not(last));
	}
	
	@Override
	public void and() {
		
		StateExpression right = stack.pop();
		StateExpression left = stack.pop();

		stack.push(new StateExpressions.And(left, right));
	}
	
	@Override
	public void or() {

		StateExpression right = stack.pop();
		StateExpression left = stack.pop();

		stack.push(new StateExpressions.Or(left, right));
	}

	@Override
	public StateExpression getResult() {
		return stack.peek();
	}
}
