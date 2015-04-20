package org.oddjob.state;

import junit.framework.TestCase;

import org.oddjob.scheduling.state.TimerState;

public class StateMatchTest extends TestCase {

	public void testLotsOfExamples() {
		
		assertEquals(true, new StateMatch(
				JobState.COMPLETE).test(JobState.COMPLETE));
		
		assertEquals(true, new StateMatch(
				ParentState.COMPLETE).test(JobState.COMPLETE));
		
		assertEquals(true, new StateMatch(
				ServiceState.STARTED).test(TimerState.STARTED));
		
		assertEquals(true, new StateMatch(
				TimerState.ACTIVE).test(TimerState.STARTED));
	}
}
