package org.oddjob.state;


import java.io.IOException;

import junit.framework.TestCase;

import org.oddjob.Helper;
import org.oddjob.framework.SimpleJob;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.MockScheduledExecutorService;

public class OrStateTest extends TestCase {

	private class Result implements JobStateListener {
		JobState result;
		
		public void jobStateChange(JobStateEvent event) {
			result = event.getJobState();
		}
	}
	
	private class UnusedServices extends MockScheduledExecutorService {
	}
	
	
	public void testComplete() {
		
		OrState test = new OrState();
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
		
		assertEquals(JobState.COMPLETE, listener.result);
		
		j2.run();
		
		assertEquals(JobState.COMPLETE, listener.result);
		
		test.setJobs(1, null);
		
		assertEquals(JobState.COMPLETE, listener.result);
		
		test.setJobs(0, null);
		
		assertEquals(JobState.READY, listener.result);
	}
	
	public void testException() {
		
		OrState test = new OrState();
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
		
		assertEquals(JobState.COMPLETE, listener.result);
		
		j2.run();
		
		assertEquals(JobState.EXCEPTION, listener.result);
		
		test.setJobs(1, null);
		
		assertEquals(JobState.EXCEPTION, listener.result);
		
		test.setJobs(0, null);
		
		assertEquals(JobState.READY, listener.result);
	}
	
	public void testManyComplete() {
		
		OrState test = new OrState();
		test.setExecutorService(new UnusedServices());
		test.run();
		
		Result listener = new Result();
		test.addJobStateListener(listener);

		assertEquals(JobState.READY, listener.result);
		
		FlagState j1 = new FlagState(JobState.INCOMPLETE);
		FlagState j2 = new FlagState(JobState.INCOMPLETE);
		FlagState j3 = new FlagState(JobState.COMPLETE);
		FlagState j4 = new FlagState(JobState.INCOMPLETE);

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
	
	public void testSerialize() throws IOException, ClassNotFoundException {
		
		DefaultExecutors services = new DefaultExecutors();
		SimpleJob notSerializable = new SimpleJob() {
			@Override
			protected int execute() throws Throwable {
				return 0;
			}
		};
		
		OrState test = new OrState();
		test.setExecutorService(services.getPoolExecutor());
		test.setJobs(0, notSerializable);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, 
				test.lastJobStateEvent().getJobState());
		
		services.stop();
		
		OrState copy = (OrState) Helper.copy(test);
		
		assertEquals(JobState.COMPLETE, copy.lastJobStateEvent().getJobState());
	}
}
