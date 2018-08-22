package org.oddjob.state;

import org.oddjob.Stateful;
import org.oddjob.jobs.structural.SequentialJob;

/**
 * Shared utility class for deciding if a {@link SequentialJob} can continue.
 * In a separate class to be shared with {@link ForEach}.
 * 
 * @author rob
 *
 */
public class SequentialHelper {

	/**
	 * Can execution continue after executing the child.
	 * 
	 * @param child The child of the sequential job.
	 * 
	 * @return true if it can, false otherwise.
	 */
	public boolean canContinueAfter(Object child) {
		State state = JobState.COMPLETE;
		if (child instanceof Stateful) {
			state = ((Stateful) child).lastStateEvent().getState();
		}
		
		StateCondition condition = StateConditions.FAILURE;
		return ! (condition.test(state));
	}	
}
