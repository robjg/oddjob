package org.oddjob.state;

import junit.framework.TestCase;

public class StateConditionsTest extends TestCase {
	
	public void testCompleteAlltates() {
		
		StateCondition test = StateConditions.COMPLETE;
		
		assertEquals(false, test.test(JobState.READY));
		assertEquals(false, test.test(JobState.EXECUTING));
		assertEquals(true, test.test(JobState.COMPLETE));
		assertEquals(false, test.test(JobState.INCOMPLETE));
		assertEquals(false, test.test(JobState.EXCEPTION));
		assertEquals(false, test.test(ParentState.READY));
		
		assertEquals(false, test.test(ServiceState.STARTABLE));
		assertEquals(false, test.test(ServiceState.STARTING));
		assertEquals(true, test.test(ServiceState.STARTED));
		assertEquals(false, test.test(ServiceState.EXCEPTION));
		assertEquals(true, test.test(ServiceState.STOPPED));
		
		assertEquals(false, test.test(ParentState.READY));
		assertEquals(false, test.test(ParentState.EXECUTING));
		assertEquals(false, test.test(ParentState.ACTIVE));
		assertEquals(true, test.test(ParentState.STARTED));
		assertEquals(true, test.test(ParentState.COMPLETE));
		assertEquals(false, test.test(ParentState.INCOMPLETE));
		assertEquals(false, test.test(ParentState.EXCEPTION));
	}
	
	public void testDoneAllStates() {
		
		StateCondition test = StateConditions.DONE;
		
		assertEquals(false, test.test(JobState.READY));
		assertEquals(false, test.test(JobState.EXECUTING));
		assertEquals(true, test.test(JobState.COMPLETE));
		assertEquals(false, test.test(JobState.INCOMPLETE));
		assertEquals(false, test.test(JobState.EXCEPTION));
		assertEquals(false, test.test(ParentState.READY));
		
		assertEquals(false, test.test(ServiceState.STARTABLE));
		assertEquals(false, test.test(ServiceState.STARTING));
		assertEquals(false, test.test(ServiceState.STARTED));
		assertEquals(false, test.test(ServiceState.EXCEPTION));
		assertEquals(true, test.test(ServiceState.STOPPED));
		
		assertEquals(false, test.test(ParentState.READY));
		assertEquals(false, test.test(ParentState.EXECUTING));
		assertEquals(false, test.test(ParentState.ACTIVE));
		assertEquals(false, test.test(ParentState.STARTED));
		assertEquals(true, test.test(ParentState.COMPLETE));
		assertEquals(false, test.test(ParentState.INCOMPLETE));
		assertEquals(false, test.test(ParentState.EXCEPTION));
	}
	
	public void testFinishedAllStates() {
		
		StateCondition test = StateConditions.FINISHED;
		
		assertEquals(false, test.test(JobState.READY));
		assertEquals(false, test.test(JobState.EXECUTING));
		assertEquals(true, test.test(JobState.COMPLETE));
		assertEquals(true, test.test(JobState.INCOMPLETE));
		assertEquals(true, test.test(JobState.EXCEPTION));
		
		assertEquals(false, test.test(ServiceState.STARTABLE));
		assertEquals(false, test.test(ServiceState.STARTING));
		assertEquals(true, test.test(ServiceState.STARTED));
		assertEquals(true, test.test(ServiceState.EXCEPTION));
		assertEquals(true, test.test(ServiceState.STOPPED));
		
		assertEquals(false, test.test(ParentState.READY));
		assertEquals(false, test.test(ParentState.EXECUTING));
		assertEquals(false, test.test(ParentState.ACTIVE));
		assertEquals(true, test.test(ParentState.STARTED));
		assertEquals(true, test.test(ParentState.COMPLETE));
		assertEquals(true, test.test(ParentState.INCOMPLETE));
		assertEquals(true, test.test(ParentState.EXCEPTION));
		
	}
	
	public void testEndedAllStates() {
		
		StateCondition test = StateConditions.ENDED;
		
		assertEquals(false, test.test(JobState.READY));
		assertEquals(false, test.test(JobState.EXECUTING));
		assertEquals(true, test.test(JobState.COMPLETE));
		assertEquals(true, test.test(JobState.INCOMPLETE));
		assertEquals(true, test.test(JobState.EXCEPTION));
		assertEquals(false, test.test(ParentState.READY));
		
		assertEquals(false, test.test(ServiceState.STARTABLE));
		assertEquals(false, test.test(ServiceState.STARTING));
		assertEquals(false, test.test(ServiceState.STARTED));
		assertEquals(true, test.test(ServiceState.EXCEPTION));
		assertEquals(true, test.test(ServiceState.STOPPED));
		
		assertEquals(false, test.test(ParentState.READY));
		assertEquals(false, test.test(ParentState.EXECUTING));
		assertEquals(false, test.test(ParentState.ACTIVE));
		assertEquals(false, test.test(ParentState.STARTED));
		assertEquals(true, test.test(ParentState.COMPLETE));
		assertEquals(true, test.test(ParentState.INCOMPLETE));
		assertEquals(true, test.test(ParentState.EXCEPTION));
	}
	
	public void testFailureAllStates() {
		
		StateCondition test = StateConditions.FAILURE;
		
		assertEquals(false, test.test(JobState.READY));
		assertEquals(false, test.test(JobState.EXECUTING));
		assertEquals(false, test.test(JobState.COMPLETE));
		assertEquals(true, test.test(JobState.INCOMPLETE));
		assertEquals(true, test.test(JobState.EXCEPTION));
		
		assertEquals(false, test.test(ServiceState.STARTABLE));
		assertEquals(false, test.test(ServiceState.STARTING));
		assertEquals(false, test.test(ServiceState.STARTED));
		assertEquals(true, test.test(ServiceState.EXCEPTION));
		assertEquals(false, test.test(ServiceState.STOPPED));
		
		assertEquals(false, test.test(ParentState.READY));
		assertEquals(false, test.test(ParentState.EXECUTING));
		assertEquals(false, test.test(ParentState.ACTIVE));
		assertEquals(false, test.test(ParentState.STARTED));
		assertEquals(false, test.test(ParentState.COMPLETE));
		assertEquals(true, test.test(ParentState.INCOMPLETE));
		assertEquals(true, test.test(ParentState.EXCEPTION));
		
	}
	
	public void testRunningAllStates() {
		
		StateCondition test = StateConditions.RUNNING;
		
		assertEquals(false, test.test(JobState.READY));
		assertEquals(true, test.test(JobState.EXECUTING));
		assertEquals(false, test.test(JobState.COMPLETE));
		assertEquals(false, test.test(JobState.INCOMPLETE));
		assertEquals(false, test.test(JobState.EXCEPTION));
		assertEquals(false, test.test(ParentState.READY));
		
		assertEquals(false, test.test(ServiceState.STARTABLE));
		assertEquals(true, test.test(ServiceState.STARTING));
		assertEquals(true, test.test(ServiceState.STARTED));
		assertEquals(false, test.test(ServiceState.EXCEPTION));
		assertEquals(false, test.test(ServiceState.STOPPED));
		
		assertEquals(false, test.test(ParentState.READY));
		assertEquals(true, test.test(ParentState.EXECUTING));
		assertEquals(true, test.test(ParentState.ACTIVE));
		assertEquals(true, test.test(ParentState.STARTED));
		assertEquals(false, test.test(ParentState.COMPLETE));
		assertEquals(false, test.test(ParentState.INCOMPLETE));
		assertEquals(false, test.test(ParentState.EXCEPTION));
	}
	
	public void testStartedAllStates() {
		
		StateCondition test = StateConditions.STARTED;
		
		assertEquals(false, test.test(JobState.READY));
		assertEquals(false, test.test(JobState.EXECUTING));
		assertEquals(false, test.test(JobState.COMPLETE));
		assertEquals(false, test.test(JobState.INCOMPLETE));
		assertEquals(false, test.test(JobState.EXCEPTION));
		
		assertEquals(false, test.test(ServiceState.STARTABLE));
		assertEquals(false, test.test(ServiceState.STARTING));
		assertEquals(true, test.test(ServiceState.STARTED));
		assertEquals(false, test.test(ServiceState.EXCEPTION));
		assertEquals(false, test.test(ServiceState.STOPPED));
		
		assertEquals(false, test.test(ParentState.READY));
		assertEquals(false, test.test(ParentState.EXECUTING));
		assertEquals(false, test.test(ParentState.ACTIVE));
		assertEquals(true, test.test(ParentState.STARTED));
		assertEquals(false, test.test(ParentState.COMPLETE));
		assertEquals(false, test.test(ParentState.INCOMPLETE));
		assertEquals(false, test.test(ParentState.EXCEPTION));		
	}
	
	public void testActiveAllStates() {
		
		StateCondition test = StateConditions.ACTIVE;
		
		assertEquals(false, test.test(JobState.READY));
		assertEquals(false, test.test(JobState.EXECUTING));
		assertEquals(false, test.test(JobState.COMPLETE));
		assertEquals(false, test.test(JobState.INCOMPLETE));
		assertEquals(false, test.test(JobState.EXCEPTION));
		
		assertEquals(false, test.test(ServiceState.STARTABLE));
		assertEquals(false, test.test(ServiceState.STARTING));
		assertEquals(false, test.test(ServiceState.STARTED));
		assertEquals(false, test.test(ServiceState.EXCEPTION));
		assertEquals(false, test.test(ServiceState.STOPPED));
		
		assertEquals(false, test.test(ParentState.READY));
		assertEquals(false, test.test(ParentState.EXECUTING));
		assertEquals(true, test.test(ParentState.ACTIVE));
		assertEquals(false, test.test(ParentState.STARTED));
		assertEquals(false, test.test(ParentState.COMPLETE));
		assertEquals(false, test.test(ParentState.INCOMPLETE));
		assertEquals(false, test.test(ParentState.EXCEPTION));		
	}
	
	public void testLiveAllStates() {
		
		StateCondition test = StateConditions.LIVE;
		
		assertEquals(false, test.test(JobState.READY));
		assertEquals(false, test.test(JobState.EXECUTING));
		assertEquals(false, test.test(JobState.COMPLETE));
		assertEquals(false, test.test(JobState.INCOMPLETE));
		assertEquals(false, test.test(JobState.EXCEPTION));
		
		assertEquals(false, test.test(ServiceState.STARTABLE));
		assertEquals(false, test.test(ServiceState.STARTING));
		assertEquals(true, test.test(ServiceState.STARTED));
		assertEquals(false, test.test(ServiceState.EXCEPTION));
		assertEquals(false, test.test(ServiceState.STOPPED));
		
		assertEquals(false, test.test(ParentState.READY));
		assertEquals(false, test.test(ParentState.EXECUTING));
		assertEquals(true, test.test(ParentState.ACTIVE));
		assertEquals(true, test.test(ParentState.STARTED));
		assertEquals(false, test.test(ParentState.COMPLETE));
		assertEquals(false, test.test(ParentState.INCOMPLETE));
		assertEquals(false, test.test(ParentState.EXCEPTION));		
	}
}
