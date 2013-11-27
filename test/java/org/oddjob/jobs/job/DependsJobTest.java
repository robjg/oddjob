/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.jobs.job;

import junit.framework.TestCase;

import org.oddjob.OddjobTestHelper;
import org.oddjob.MockStateful;
import org.oddjob.framework.SimpleJob;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.StateListener;
import org.oddjob.state.State;
import org.oddjob.state.StateCondition;

/**
 *
 */
public class DependsJobTest extends TestCase {

	class TestJob extends SimpleJob {
		boolean ran;
		public int execute() {
			ran = true;
			return 0;
		}
	}
	
	/**
	 * Test that a ready job is run. 
	 *
	 */
	public void testReady() {
		TestJob testJob = new TestJob();
		DependsJob j = new DependsJob();
		j.setJob(testJob);
		j.run();
		
		assertTrue(testJob.ran);
		assertEquals("Complete", JobState.COMPLETE, OddjobTestHelper.getJobState(j));
	}

	/**
	 * Test that a complete job isn't run. 
	 *
	 */
	public void testAlreadyComplete() {
		TestJob testJob = new TestJob();
		testJob.run();
		testJob.ran = false;
		assertEquals(JobState.COMPLETE, testJob.lastStateEvent().getState());
		
		DependsJob j = new DependsJob();
		j.setJob(testJob);
		j.run();
		
		assertFalse(testJob.ran);
		assertEquals("Complete", JobState.COMPLETE, OddjobTestHelper.getJobState(j));
	}

	private void setState(final JobStateHandler handler, 
			final JobState state) {
		boolean ran = handler.waitToWhen(new StateCondition() {
			public boolean test(State state) {
				return true;
			}
		}, new Runnable() {
			public void run() {
				handler.setState(state);
				handler.fireEvent();
			}
		});
		assertTrue(ran);
	}
	
	/**
	 * Test that an executing job is waited for. 
	 *
	 */
	public void testExecuting() throws InterruptedException {
		class Executing extends MockStateful {
			JobStateHandler h = new JobStateHandler(this);
			public void addStateListener(StateListener listener) {
				h.addStateListener(listener);
			}
			public void removeStateListener(StateListener listener) {
				h.removeStateListener(listener);
			}
		}
		Executing testJob = new Executing();
		setState(testJob.h, JobState.EXECUTING);
		
		DependsJob j = new DependsJob();
		j.setJob(testJob);
		Thread t = new Thread(j);
		t.start();
		
		while (JobState.EXECUTING != OddjobTestHelper.getJobState(j)) {
			Thread.yield();
		}
		
		setState(testJob.h, JobState.INCOMPLETE);
		
		t.join();
		assertEquals(JobState.INCOMPLETE, OddjobTestHelper.getJobState(j));
	}
}
