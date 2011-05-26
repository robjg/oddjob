package org.oddjob.state;

/**
 * Implementation of a {@link StateOperator} that provides logical 'and'
 * like functionality.
 * 
 * @author rob
 *
 */
public class OrStateOp implements StateOperator {
	
	private static final JobState[][] or = { 
		 {JobState.READY, JobState.EXECUTING, JobState.INCOMPLETE, JobState.COMPLETE, JobState.EXCEPTION}, 
		 {JobState.EXECUTING, JobState.EXECUTING, JobState.INCOMPLETE, JobState.COMPLETE, JobState.EXCEPTION}, 
		 {JobState.INCOMPLETE, JobState.INCOMPLETE, JobState.INCOMPLETE, JobState.COMPLETE, JobState.EXCEPTION}, 
		 {JobState.COMPLETE, JobState.COMPLETE, JobState.COMPLETE, JobState.COMPLETE, JobState.EXCEPTION},
		 {JobState.EXCEPTION, JobState.EXCEPTION, JobState.EXCEPTION, JobState.EXCEPTION, JobState.EXCEPTION} 
	};
	 
	public JobState evaluate(JobState... states) {
		new AssertNonDestroyed().evaluate(states);
		
		JobState state = JobState.READY;
		
		if (states.length > 0 ) {			
			state = states[0];
			
			for (int i = 1; i < states.length; ++i) {
				state = or[state.ordinal()][states[i].ordinal()]; 
			}
		}
		
		return state;
	}
	
}
