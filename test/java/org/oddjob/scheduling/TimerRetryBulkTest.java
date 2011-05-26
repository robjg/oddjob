package org.oddjob.scheduling;

import junit.framework.TestCase;

import org.oddjob.framework.SimpleJob;
import org.oddjob.jobs.WaitJob;
import org.oddjob.schedules.schedules.NowSchedule;

/**
 * Bulk test.
 * 
 */
public class TimerRetryBulkTest extends TestCase {

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
		wait.setState("INCOMPLETE");
		wait.setFor(test);
		
		wait.run();
		
		assertEquals(COUNT_TO + 1, job.count);
		
		services.stop();
	}
	
	private class OurOtherJob extends SimpleJob {
		int count;
		@Override
		protected int execute() throws Throwable {
			if (++count > COUNT_TO) {
				return 0;
			}
			else {
				return 1;
			}
		}
	}
	
	public void testRetryManyTimes() {
		
		DefaultExecutors services = new DefaultExecutors();
		
		Retry test = new Retry();
		test.setSchedule(new NowSchedule());
		test.setScheduleExecutorService(
				services.getScheduledExecutor());
		
		OurOtherJob job = new OurOtherJob();
		test.setJob(job);
		
		test.run();

		WaitJob wait = new WaitJob();
		wait.setState("COMPLETE");
		wait.setFor(test);
		
		wait.run();
		
		assertEquals(COUNT_TO + 1, job.count);
		
		services.stop();
	}
}
