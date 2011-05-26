package org.oddjob.state;

public class AssertNonDestroyed implements StateOperator {

	@Override
	public JobState evaluate(JobState... states) throws IllegalStateException {

		for (int i = 0; i < states.length; ++i) {
			if (states[i] == JobState.DESTROYED) {
				throw new IllegalStateException(
						"DESTROYED state is not valid as operand " + i + 
						" of a StateOperator.");
			}
		}
		return null;
	}
}
