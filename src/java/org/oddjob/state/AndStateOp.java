package org.oddjob.state;

/**
 * Implementation of a {@link StateOperator} that provides logical 'and'
 * like functionality.
 * 
 * @author rob
 *
 */
public class AndStateOp implements StateOperator {
	
	 private static final JobState[][] and = { 
		 {JobState.READY, JobState.READY, JobState.READY, JobState.READY, JobState.EXCEPTION}, 
		 {JobState.READY, JobState.EXECUTING, JobState.READY, JobState.READY, JobState.EXCEPTION}, 
		 {JobState.READY, JobState.READY, JobState.INCOMPLETE, JobState.READY, JobState.EXCEPTION}, 
		 {JobState.READY, JobState.READY, JobState.READY, JobState.COMPLETE, JobState.EXCEPTION},
		 {JobState.EXCEPTION, JobState.EXCEPTION, JobState.EXCEPTION, JobState.EXCEPTION, JobState.EXCEPTION} 
	};
	 
	public JobState evaluate(JobState... states) {
		new AssertNonDestroyed().evaluate(states);
		
		JobState state = JobState.READY;
		
		if (states.length > 0 ) {			
			state = states[0];
			
			for (int i = 1; i < states.length; ++i) {
				state = and[state.ordinal()][states[i].ordinal()]; 
			}
		}
		
		return state;
	}
	
}
