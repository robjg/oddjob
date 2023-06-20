package org.oddjob.state;

import org.oddjob.jobs.job.RunJob;

/**
 * Handle a destroyed state. Most {@link StateOperator}s don't need to
 * handle the destroyed state because the Arooa Framework ensures that
 * a child is removed from a parent before it is destroyed. There are
 * however situations where a job adds it's own children and must cope
 * with them being destroyed. {@link RunJob} is one such example.
 * 
 * @author rob
 *
 */
public class DestroyHandlingStateOp implements StateOperator {

	private final StateOperator delegate;
	
	public DestroyHandlingStateOp(StateOperator delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public StateEvent evaluate(StateEvent... states) {

		for (int i = 0; i < states.length; ++i) {
			if (states[i].getState().isDestroyed()) {
				return onDestroyed(i);
			}
		}
		return delegate.evaluate(states);
		
		
	}
	
	protected StateEvent onDestroyed(int index) {
		return ConstStateful.event(ParentState.EXCEPTION);
	}
}
