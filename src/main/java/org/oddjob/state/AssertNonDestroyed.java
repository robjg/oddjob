package org.oddjob.state;

/**
 * A {@link StateOperator} that ensures non of the operands
 * are destroyed.
 * 
 * @author rob
 *
 */
public class AssertNonDestroyed implements StateOperator {

	@Override
	public ParentState evaluate(State... states) throws IllegalStateException {

		for (int i = 0; i < states.length; ++i) {
			if (states[i].isDestroyed()) {
				throw new IllegalStateException(
						"DESTROYED state is not valid as operand " + i + 
						" of a StateOperator.");
			}
		}
		return null;
	}
}
