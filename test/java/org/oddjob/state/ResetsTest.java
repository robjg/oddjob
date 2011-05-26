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
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());

		test.softReset();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());

	}
	
	public void testNormal() {
		
		Resets test = new Resets();
		test.setSoften(false);
		test.setHarden(false);
		
		FlagState job = new FlagState(JobState.COMPLETE);
		
		test.setJob(job);
		
		test.run();
		
		test.softReset();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		test.hardReset();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());

	}
	
}
