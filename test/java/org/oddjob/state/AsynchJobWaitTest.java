package org.oddjob.state;

import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;

import org.oddjob.Stateful;
import org.oddjob.jobs.structural.ParallelJob;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.OddjobLockedException;

import org.oddjob.OjTestCase;

public class AsynchJobWaitTest extends OjTestCase {

   @Test
	public void testGivenSimpleJobWhenRunThenNoWait() {

		AsynchJobWait test = new AsynchJobWait();
		
		FlagState job = new FlagState();
		boolean asynchronous = test.runAndWaitWith(job);

		assertFalse(asynchronous);
		
		assertEquals(JobState.COMPLETE, job.lastStateEvent().getState());
	}
	
	private class SlowToStartJob implements Stateful, Runnable {
		
		StateHandler<JobState> states = new StateHandler<JobState>(
				this, JobState.READY);

		
		void fireJobState(final JobState state) {
			try {
				states.tryToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						states.setState(state);
						states.fireEvent();
					}
				});
			}
			catch (OddjobLockedException e) {
				fail(e.getMessage());
			}
		}
		
		@Override
		public void addStateListener(StateListener listener) {
			states.addStateListener(listener);
		}
		
		@Override
		public void removeStateListener(StateListener listener) {
			states.removeStateListener(listener);
		}
		
		@Override
		public StateEvent lastStateEvent() {
			throw new RuntimeException("Unexpected");
		}
		
		@Override
		public void run() {
		}
	}

   @Test
	public void testSlowToStartJobBlocks() {
		
		AsynchJobWait test = new AsynchJobWait();
		
		final SlowToStartJob job = new SlowToStartJob();
	
		Timer timer = new Timer();
		
		long timeNow = System.currentTimeMillis();
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				job.fireJobState(JobState.EXECUTING);
			}
		}, 100);
		
		boolean asynchronous = test.runAndWaitWith(job);

		timer.cancel();
		
		assertEquals(true, System.currentTimeMillis() > timeNow + 99);
		
		assertTrue(asynchronous);
	}
	
   @Test
	public void testGivenParallelJobThenAsync() throws InterruptedException {
		
		FlagState job = new FlagState(JobState.COMPLETE);

		DefaultExecutors defaultServices = new DefaultExecutors();
		
		ParallelJob parallel = new ParallelJob();
		
		StateSteps steps = new StateSteps(parallel);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		parallel.setExecutorService(defaultServices.getPoolExecutor());

		parallel.setJobs(0, job);
		
		AsynchJobWait test = new AsynchJobWait();
		
		boolean asynchronous = test.runAndWaitWith(parallel);
		
		assertTrue(asynchronous);
		
		steps.checkWait();
		
		defaultServices.stop();
	}
	
   @Test
	public void testParallelChildWithJoin() {
		
		FlagState job = new FlagState(JobState.COMPLETE);

		DefaultExecutors defaultServices = new DefaultExecutors();
		
		ParallelJob parallel = new ParallelJob();
		
		StateSteps steps = new StateSteps(parallel);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		parallel.setExecutorService(defaultServices.getPoolExecutor());

		parallel.setJobs(0, job);
		
		AsynchJobWait test = new AsynchJobWait();
		test.setJoin(true);
		
		boolean asynchronous = test.runAndWaitWith(parallel);
		
		assertFalse(asynchronous);
		
		steps.checkNow();
		
		defaultServices.stop();
		
	}
}
