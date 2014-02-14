package org.oddjob.scheduling;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.framework.SimpleJob;
import org.oddjob.jobs.WaitJob;
import org.oddjob.schedules.schedules.NowSchedule;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateConditions;
import org.oddjob.tools.StateSteps;

/**
 * Bulk test.
 * 
 */
public class TimerRetryBulkTest extends TestCase {

	private static final Logger logger = 
			Logger.getLogger(TimerRetryBulkTest.class);

	private static final int COUNT_TO = 1000;
	
	private class OurJob extends SimpleJob {
		int count;
		@Override
		protected int execute() throws Throwable {
			if (++count > COUNT_TO) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.debug("----------------- " + getName() + " -------------");
	}
			
	public void testTimerManyTimes() {
		
		DefaultExecutors services = new DefaultExecutors();
		
		Timer test = new Timer();
		test.setSchedule(new NowSchedule());
		test.setHaltOnFailure(true);
		test.setScheduleExecutorService(
				services.getScheduledExecutor());
		
		OurJob job = new OurJob();
		test.setJob(job);
		
		test.run();

		WaitJob wait = new WaitJob();
		wait.setState(StateConditions.INCOMPLETE);
		wait.setFor(test);
		
		wait.run();
		
		assertEquals(COUNT_TO + 1, job.count);
		
		services.stop();
	}
	
	private class OurOtherJob extends SimpleJob {
		private final AtomicInteger count = new AtomicInteger();
		@Override
		protected int execute() throws Throwable {
			if (count.incrementAndGet() > COUNT_TO) {
				logger().info("Count " + count + ", returning 0.");
				return 0;
			}
			else {
				logger().info("Count " + count + ", returning 1.");
				return 1;
			}
		}
	}
	
	public void testRetryManyTimes() throws InterruptedException {
		
		DefaultExecutors services = new DefaultExecutors();
		
		Retry test = new Retry();
		test.setSchedule(new NowSchedule());
		test.setScheduleExecutorService(
				services.getScheduledExecutor());
		
		OurOtherJob job = new OurOtherJob();
		test.setJob(job);
		
		StateSteps testStates = new StateSteps(test);
		testStates.setTimeout(20 * 1000L);
		testStates.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.STARTED, ParentState.COMPLETE);

		test.run();

		testStates.checkWait();
		
		assertEquals(COUNT_TO + 1, job.count.get());
		
		services.stop();
	}
}
