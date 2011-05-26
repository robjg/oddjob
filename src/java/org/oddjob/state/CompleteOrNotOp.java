package org.oddjob.state;


/**
 * Implementation of a {@link StateOperator} that is
 * either complete if all the children are complete, or not.
 * <p>
 * This is used by scheduling.
 * 
 * @author rob
 *
 */
public class CompleteOrNotOp implements StateOperator {
	
	public JobState evaluate(JobState... states) {
		for (JobState state: states) {
			if (state == JobState.DESTROYED) {
				throw new IllegalArgumentException("Illegal operand state " +
						JobState.DESTROYED + ".");
			}
			if (state == JobState.EXECUTING) {
				return JobState.EXECUTING;
			}
			if (state == JobState.READY) {
				return JobState.COMPLETE;
			}
			if (state != JobState.COMPLETE) {
				return JobState.INCOMPLETE;
			}
		}
		return JobState.COMPLETE;
	}
}
