package org.oddjob.scheduling;

import java.util.Date;
import java.util.concurrent.Exchanger;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.StateSteps;
import org.oddjob.Stoppable;
import org.oddjob.framework.SimpleJob;
import org.oddjob.jobs.job.StopJob;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;

public class TimerStopTest extends TestCase {

	private static final Logger logger = Logger.getLogger(TimerStopTest.class);
	
	@Override
	protected void setUp() throws Exception {
		logger.info("----------------  " + getName() + "  ---------------");
	}
	
	private class NeverRan extends SimpleJob {
		@Override
		protected int execute() throws Throwable {
			throw new RuntimeException("Unexpected.");
		}
	}
	
	public void testStopBeforeRunning() throws InterruptedException, FailedToStopException {
		DefaultExecutors services = new DefaultExecutors();
		
		Timer test = new Timer();
		test.setScheduleExecutorService(
				services.getScheduledExecutor());
		test.setSchedule(new Schedule() {
			
			public IntervalTo nextDue(ScheduleContext context) {
				return new IntervalTo(new Date(Long.MAX_VALUE));
			}
		});
		
		NeverRan job = new NeverRan();
				
		test.setJob(job);
	
		test.run();
		
		test.stop();
		
		assertEquals(JobState.READY, 
				job.lastStateEvent().getState());
		
		assertEquals(ParentState.COMPLETE, 
				test.lastStateEvent().getState());
		
		services.stop();
	}
	
	private static class RunningJob extends SimpleJob 
	implements Stoppable{
		
		Exchanger<Void> running = new Exchanger<Void>();
		
		Thread t;
		
		@Override
		protected int execute() throws Throwable {
			t = Thread.currentThread();
			running.exchange(null);
			synchronized (this) {
				try {
					logger.debug("Running job waiting.");
					wait();
				}
				catch (InterruptedException e) {
					logger.debug("Running job itnerrupted.");
					Thread.currentThread().interrupt();
				}
			};
			return 0;
		}

		@Override
		protected void onStop() {
			synchronized (this) {
				t.interrupt();
			}
		}
	}	
	
	public void testStopWhenRunning() throws InterruptedException, FailedToStopException {
		DefaultExecutors services = new DefaultExecutors();
		
		Timer test = new Timer();
		test.setScheduleExecutorService(
				services.getScheduledExecutor());
		test.setSchedule(new Schedule() {
			
			public IntervalTo nextDue(ScheduleContext context) {
				return new IntervalTo(new Date());
			}
		});
		
		RunningJob job = new RunningJob();
				
		test.setJob(job);
	
		test.run();
		
		job.running.exchange(null);
		
		test.stop();

		assertFalse(Thread.interrupted());
		
		assertEquals(ParentState.COMPLETE, 
				test.lastStateEvent().getState());
		
		assertEquals(JobState.COMPLETE, 
				job.lastStateEvent().getState());
		
		services.stop();
	}
	
	private static class QuickJob extends SimpleJob {
		
		@Override
		protected int execute() throws Throwable {
			return 0;
		}
	}	
	
	public void testStopBetweenSchedules() throws InterruptedException {
		DefaultExecutors services = new DefaultExecutors();
		services.setPoolSize(5);
		
		final Timer test = new Timer();
		test.setScheduleExecutorService(
				services.getScheduledExecutor());
		test.setSchedule(new Schedule() {
			
			public IntervalTo nextDue(ScheduleContext context) {
				if (context.getData("done") == null) {
					context.putData("done", new Object());
					return new IntervalTo(new Date());
				}
				else {
					return new IntervalTo(new Date(Long.MAX_VALUE));					
				}
			}
		});
		
		QuickJob job = new QuickJob();
		
		StopJob stop = new StopJob();
		stop.setJob(test);
		
		Trigger trigger = new Trigger();
		trigger.setExecutorService(services.getPoolExecutor());
		trigger.setJob(stop);
		trigger.setOn(job);

		trigger.run();
				
		test.setJob(job);
	
		StateSteps state = new StateSteps(test);
		
		state.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE, ParentState.COMPLETE);
		
		test.run();
		
		state.checkWait();
		
		assertEquals(JobState.COMPLETE, 
				job.lastStateEvent().getState());
		
		services.stop();
	}
}
