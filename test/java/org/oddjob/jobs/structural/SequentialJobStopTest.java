package org.oddjob.jobs.structural;
import org.junit.Before;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.framework.SimpleJob;
import org.oddjob.framework.StopWait;
import org.oddjob.images.IconHelper;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.tools.IconSteps;
import org.oddjob.tools.StateSteps;


public class SequentialJobStopTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(SequentialJobStopTest.class);
	
    @Before
    public void setUp() throws Exception {

		
		logger.info("--------------------------------  " + getName() +
				"  --------------------------------");
	}
	
   @Test
	public void testStopBeforeRun() throws FailedToStopException {
		
		FlagState job1 = new FlagState();
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, job1);
		
		IconSteps testIcons = new IconSteps(test);
		testIcons.startCheck(IconHelper.READY);
		
		test.stop();
		assertEquals(false, test.isStop());
		
		testIcons.checkNow();
		
		test.run();
		
		assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
		assertEquals(false, test.isStop());
	}
	
	public static class NoneStoppingJob extends SimpleJob 
	implements Stoppable {

		CountDownLatch latch = new CountDownLatch(1);
		
		private long stopWaitTimeout = 1;
		
		@Override
		protected int execute() throws Throwable {
			latch.await();
			return 0;
		}
		
		@Override
		protected void onReset() {
			super.onReset();
			latch = new CountDownLatch(1);
		}
		
		@Override
		protected void onStop() throws FailedToStopException {
			super.onReset();
			new StopWait(this, stopWaitTimeout).run();
		}
		
		public void setReallyStop(String anything) {
			latch.countDown();
		}

		public long getStopWaitTimeout() {
			return stopWaitTimeout;
		}

		public void setStopWaitTimeout(long stopWaitTimeout) {
			this.stopWaitTimeout = stopWaitTimeout;
		}
	}
	
	
   @Test
	public void testWithNoneStoppingChildren() throws InterruptedException {
	
		NoneStoppingJob job1 = new NoneStoppingJob();
		
		FlagState job2 = new FlagState();
		
		SequentialJob test = new SequentialJob();
			
		test.setJobs(0, job1);
		test.setJobs(1,  job2);
		
		StateSteps unstoppedState = new StateSteps(job1);
		unstoppedState.startCheck(JobState.READY, JobState.EXECUTING);
		
		Thread t = new Thread(test);
		t.start();
		
		unstoppedState.checkWait();
		
		IconSteps testIcons = new IconSteps(test);
		testIcons.startCheck(IconHelper.EXECUTING, IconHelper.STOPPING);
		
		try {
			test.stop();
			fail("Should fail.");
		}
		catch (FailedToStopException e) {
			// expected
		}
		
		testIcons.checkNow();
		
		testIcons.startCheck(IconHelper.STOPPING, 
				IconHelper.READY);
		
		job1.setReallyStop(null);
		
		testIcons.checkWait();
		
		assertEquals(false, test.isStop());
		
		assertEquals(JobState.READY, job2.lastStateEvent().getState());
	}
	
   @Test
	public void testStopNonStoppingChildWhenChildStartedDirectly() throws InterruptedException {
		
		NoneStoppingJob job1 = new NoneStoppingJob();
		
		SequentialJob test = new SequentialJob();
			
		test.setJobs(0, job1);
		
		StateSteps unstoppedState = new StateSteps(job1);
		unstoppedState.startCheck(JobState.READY, JobState.EXECUTING);
		
		Thread t = new Thread(job1);
		t.start();
		
		unstoppedState.checkWait();
		
		IconSteps testIcons = new IconSteps(test);
		testIcons.startCheck(IconHelper.READY);
		
		try {
			test.stop();
			fail("Should fail.");
		}
		catch (FailedToStopException e) {
			// expected
		}
		assertEquals(false, test.isStop());
		
		testIcons.checkNow();
		
		testIcons.startCheck(
				IconHelper.READY);
		
		unstoppedState.startCheck(JobState.EXECUTING, JobState.COMPLETE);
		
		job1.setReallyStop(null);
		
		unstoppedState.checkWait();
		
		testIcons.checkWait();		
	}
}
