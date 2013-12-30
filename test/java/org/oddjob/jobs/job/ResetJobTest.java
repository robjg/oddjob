package org.oddjob.jobs.job;

import junit.framework.TestCase;

import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;

public class ResetJobTest extends TestCase {

	public void testReset() {
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		job.run();
		
		assertEquals(JobState.INCOMPLETE, job.lastStateEvent().getState());
		
		ResetJob test = new ResetJob();
		
		test.setJob(job);
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		assertEquals(JobState.READY, job.lastStateEvent().getState());
		
		job.setState(JobState.COMPLETE);
		
		job.run();
		
		assertEquals(JobState.COMPLETE, job.lastStateEvent().getState());
		
		test.hardReset();
		test.setLevel("HARD");
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		assertEquals(JobState.READY, job.lastStateEvent().getState());
		
	}
}
