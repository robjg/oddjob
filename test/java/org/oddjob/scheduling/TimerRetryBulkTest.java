package org.oddjob.scheduling;
import org.junit.Before;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.framework.SimpleJob;
import org.oddjob.jobs.WaitJob;
import org.oddjob.schedules.schedules.NowSchedule;
import org.oddjob.scheduling.state.TimerState;
import org.oddjob.state.StateConditions;
import org.oddjob.tools.StateSteps;

/**
 * Bulk test.
 * 
 */
public class TimerRetryBulkTest extends OjTestCase {

	private static final Logger logger = 
			LoggerFactory.getLogger(TimerRetryBulkTest.class);

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
	
    @Before
    public void setUp() throws Exception {

		logger.debug("----------------- " + getName() + " -------------");
	}
			
   @Test
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
	
   @Test
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
		testStates.startCheck(TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.ACTIVE, TimerState.COMPLETE);

		test.run();

		testStates.checkWait();
		
		assertEquals(COUNT_TO + 1, job.count.get());
		
		services.stop();
	}
}
