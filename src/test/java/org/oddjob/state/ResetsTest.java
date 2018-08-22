package org.oddjob.state;

import org.junit.Test;

import org.oddjob.OjTestCase;

public class ResetsTest extends OjTestCase {

   @Test
	public void testReverse() {
		
		Resets test = new Resets();
		test.setSoften(true);
		test.setHarden(true);
		
		
		FlagState job = new FlagState(JobState.COMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		test.hardReset();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());

		test.softReset();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());

	}
	
   @Test
	public void testNormal() {
		
		Resets test = new Resets();
		test.setSoften(false);
		test.setHarden(false);
		
		FlagState job = new FlagState(JobState.COMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		test.softReset();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		test.hardReset();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
	}	
}
