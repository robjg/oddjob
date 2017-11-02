package org.oddjob.state;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.scheduling.state.TimerState;

public class StateMatchTest extends OjTestCase {

   @Test
	public void testLotsOfExamples() {
		
		assertEquals(true, new StateMatch(
				JobState.COMPLETE).test(JobState.COMPLETE));
		
		assertEquals(true, new StateMatch(
				ParentState.COMPLETE).test(JobState.COMPLETE));
		
		assertEquals(true, new StateMatch(
				ServiceState.STARTED).test(TimerState.STARTED));
		
		assertEquals(false, new StateMatch(
				TimerState.ACTIVE).test(TimerState.STARTED));
	}
}
