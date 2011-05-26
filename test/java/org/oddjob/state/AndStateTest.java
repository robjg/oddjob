package org.oddjob.state;


import junit.framework.TestCase;

import org.oddjob.scheduling.MockScheduledExecutorService;

public class AndStateTest extends TestCase {

	private class Result implements JobStateListener {
		JobState result;
		
		public void jobStateChange(JobStateEvent event) {
			result = event.getJobState();
		}
	}
	
	private class UnusedServices 
	extends MockScheduledExecutorService {
		
	}
	
	
	public void testComplete() {
		
		AndState test = new AndState();
		test.setExecutorService(new UnusedServices());
		test.run();
		
		Result listener = new Result();
		test.addJobStateListener(listener);

		
		assertEquals(JobState.READY, listener.result);
		
		FlagState j1 = new FlagState(JobState.COMPLETE);

		test.setJobs(0, j1);
		
		assertEquals(JobState.READY, listener.result);
		
		j1.run();
		
		assertEquals(JobState.COMPLETE, listener.result);
		
		FlagState j2 = new FlagState(JobState.COMPLETE);

		test.setJobs(0, j2);
		
		assertEquals(JobState.READY, listener.result);
		
		j2.run();
		
		assertEquals(JobState.COMPLETE, listener.result);
		
		test.setJobs(1, null);
		
		assertEquals(JobState.COMPLETE, listener.result);
		
		test.setJobs(0, null);
		
		assertEquals(JobState.READY, listener.result);
	}
	
	public void testException() {
		
		AndState test = new AndState();
		test.setExecutorService(new UnusedServices());
		test.run();
		
		Result listener = new Result();
		test.addJobStateListener(listener);

		assertEquals(JobState.READY, listener.result);
		
		FlagState j1 = new FlagState(JobState.COMPLETE);

		test.setJobs(0, j1);
		
		assertEquals(JobState.READY, listener.result);
		
		j1.run();
		
		assertEquals(JobState.COMPLETE, listener.result);
		
		FlagState j2 = new FlagState(JobState.EXCEPTION);

		test.setJobs(0, j2);
		
		assertEquals(JobState.READY, listener.result);
		
		j2.run();
		
		assertEquals(JobState.EXCEPTION, listener.result);
		
		test.setJobs(1, null);
		
		assertEquals(JobState.EXCEPTION, listener.result);
		
		test.setJobs(0, null);
		
		assertEquals(JobState.READY, listener.result);
	}
	
	public void testManyComplete() {
		
		AndState test = new AndState();
		test.setExecutorService(new UnusedServices());
		test.run();
		
		Result listener = new Result();
		test.addJobStateListener(listener);

		assertEquals(JobState.READY, listener.result);
		
		FlagState j1 = new FlagState(JobState.COMPLETE);
		FlagState j2 = new FlagState(JobState.COMPLETE);
		FlagState j3 = new FlagState(JobState.COMPLETE);
		FlagState j4 = new FlagState(JobState.COMPLETE);

		j1.run();
		j2.run();
		j3.run();
		j4.run();
		
		test.setJobs(0, j1);
		test.setJobs(1, j2);
		test.setJobs(2, j3);
		test.setJobs(3, j4);
		
		assertEquals(JobState.COMPLETE, listener.result);
		
	}
	
}
