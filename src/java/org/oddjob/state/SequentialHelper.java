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

	private class StateCatcher implements StateListener {
		
		private State jobState;
		
		public State stateFor(Object object) {
			if (!(object instanceof Stateful)) {
				jobState = JobState.READY;
			}
			else {
				Stateful stateful = (Stateful) object;
				stateful.addStateListener(this);
				stateful.removeStateListener(this);
			}
			return jobState;
		}
		
		@Override
		public void jobStateChange(StateEvent event) {
			jobState = event.getState();
		}
	}
	
	/**
	 * Can execution continue after executing the child.
	 * 
	 * @param child The child of the sequential job.
	 * 
	 * @return true if it can, false otherwise.
	 */
	public boolean canContinueAfter(Object child) {
		return new IsContinueable().test(
				new StateCatcher().stateFor(child));
	}	
}
