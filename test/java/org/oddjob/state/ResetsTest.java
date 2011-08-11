package org.oddjob.state;

import junit.framework.TestCase;

public class ResetsTest extends TestCase {

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
