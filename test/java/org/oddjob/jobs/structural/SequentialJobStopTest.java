package org.oddjob.jobs.structural;

import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.IconSteps;
import org.oddjob.StateSteps;
import org.oddjob.Stoppable;
import org.oddjob.framework.SimpleJob;
import org.oddjob.images.IconHelper;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;


public class SequentialJobStopTest extends TestCase {

	private static final Logger logger = Logger.getLogger(SequentialJobStopTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("--------------------------------  " + getName() +
				"  --------------------------------");
	}
	
	public void testStopBeforeRun() throws FailedToStopException {
		
		FlagState job1 = new FlagState();
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, job1);
		
		IconSteps testIcons = new IconSteps(test);
		testIcons.startCheck(IconHelper.READY, IconHelper.STOPPING,
				IconHelper.READY);
		
		test.stop();
		
		testIcons.checkNow();
		
		test.run();
		
		assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
		assertEquals(false, test.isStop());
	}
	
	private static class NoneStoppingJob extends SimpleJob 
	implements Stoppable {

		CountDownLatch latch = new CountDownLatch(1);
		
		@Override
		protected int execute() throws Throwable {
			latch.await();
			return 0;
		}
		
		@Override
		protected void onStop() throws FailedToStopException {
			throw new FailedToStopException(this);
		}
	}
	
	
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
		testIcons.startCheck(IconHelper.EXECUTING, IconHelper.STOPPING, 
				IconHelper.EXECUTING);
		
		try {
			test.stop();
			fail("Should fail.");
		}
		catch (FailedToStopException e) {
			// expected
		}
		
		testIcons.checkWait();
		
		testIcons.startCheck(
				IconHelper.EXECUTING, IconHelper.COMPLETE);
		
		job1.latch.countDown();
		
		testIcons.checkWait();
		
		assertEquals(false, test.isStop());
	}
}
