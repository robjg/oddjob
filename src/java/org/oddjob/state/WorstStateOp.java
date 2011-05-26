package org.oddjob.state;

import org.oddjob.Structural;

/**
 * Implementation of a {@link StateOperator} that provides the worst
 * of any given states. This Operator is used in many {@link Structural}
 * jobs to calculate parent state.
 * 
 * @author rob
 *
 */
public class WorstStateOp implements StateOperator {
	
	private static final JobState[][] worst = { 
		 {JobState.READY,      JobState.EXECUTING,  JobState.INCOMPLETE, JobState.READY,      JobState.EXCEPTION}, 
		 {JobState.EXECUTING,  JobState.EXECUTING,  JobState.EXECUTING, JobState.EXECUTING,  JobState.EXECUTING}, 
		 {JobState.INCOMPLETE, JobState.EXECUTING, JobState.INCOMPLETE, JobState.INCOMPLETE, JobState.EXCEPTION}, 
		 {JobState.READY,      JobState.EXECUTING,  JobState.INCOMPLETE, JobState.COMPLETE,   JobState.EXCEPTION},
		 {JobState.EXCEPTION,  JobState.EXECUTING,  JobState.EXCEPTION,  JobState.EXCEPTION,  JobState.EXCEPTION} 
	};
	 
	public JobState evaluate(JobState... states) {
		new AssertNonDestroyed().evaluate(states);
		
		JobState state = JobState.READY;
		
		if (states.length > 0 ) {
			state = states[0];
			for (int i = 1; i < states.length; ++i) {
				state = worst[state.ordinal()][states[i].ordinal()]; 
			}
		}
		
		return state;
	}
	
}
