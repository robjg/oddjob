package org.oddjob.scheduling;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.OddjobTestHelper;
import org.oddjob.MockOddjobServices;
import org.oddjob.MockStateful;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.framework.StopWait;
import org.oddjob.jobs.SequenceJob;
import org.oddjob.jobs.structural.ParallelJob;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.schedules.CountSchedule;
import org.oddjob.schedules.schedules.DateSchedule;
import org.oddjob.schedules.schedules.IntervalSchedule;
import org.oddjob.schedules.schedules.NowSchedule;
import org.oddjob.schedules.schedules.TimeSchedule;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.Resets;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.util.Clock;

public class RetryTest extends TestCase {

	private class OurClock implements Clock {

		Date date;
		
		public OurClock() {
		}
		
		public OurClock(String text) throws ParseException {
			date = DateHelper.parseDateTime(text);
		}
		
		public Date getDate() {
			
			return date;
		}
	}
	
	private class OurOddjobServices extends MockOddjobServices {
		
		Runnable runnable;
		long delay;
		
		boolean canceled;
		
		public ScheduledExecutorService getScheduledExecutor() {
			return new MockScheduledExecutorService() {
				
				public ScheduledFuture<?> schedule(Runnable runnable, long delay,
						TimeUnit unit) {

					OurOddjobServices.this.delay = delay;
					OurOddjobServices.this.runnable = runnable;
					
					return new MockScheduledFuture<Void>() {
						public boolean cancel(boolean interrupt) {
							canceled = true;
							return true;
						}
					};
				}
			};
		}
		
	}
	
	public void testRetry() throws ParseException, FailedToStopException {
		
		FlagState job = new FlagState();
		job.setState(JobState.INCOMPLETE);
		
		TimeSchedule time = new TimeSchedule();
		time.setFrom("14:45");
		time.setTo("15:00");

		Interval limits = time.nextDue(
				new ScheduleContext(
						DateHelper.parseDateTime("2009-02-10 12:00")));
		
		IntervalSchedule retry = new IntervalSchedule();
		retry.setInterval("00:05");
		
		OurClock clock = new OurClock();
		clock.date = DateHelper.parseDateTime("2009-02-10 14:50");

		Retry test = new Retry();
		test.setLimits(limits);
		test.setSchedule(retry);
		test.setClock(clock);
		test.setJob(job);
		
		OurOddjobServices oddjobServices = new OurOddjobServices();
		
		test.setScheduleExecutorService(
				oddjobServices.getScheduledExecutor());
		
		test.run();
		
		assertNotNull(oddjobServices.runnable);
		
		assertEquals(0, oddjobServices.delay);		
		assertEquals(
				DateHelper.parseDateTime("2009-02-10 14:50"),
				test.getNextDue());

		oddjobServices.delay = -1;
		clock.date = DateHelper.parseDateTime("2009-02-10 14:53");
		
		oddjobServices.runnable.run();

		assertEquals(ParentState.STARTED, test.lastStateEvent().getState());
		
		assertEquals(2 * 60 * 1000, oddjobServices.delay);
		assertEquals(
				DateHelper.parseDateTime("2009-02-10 14:55"),
				test.getNextDue());
		
		oddjobServices.delay = -1;
		
		oddjobServices.runnable.run();
		
		assertEquals(-1, oddjobServices.delay);
		assertEquals(null,
				test.getNextDue());
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
		
		test.stop();
		
		assertFalse(oddjobServices.canceled);
	}
	
	/**
	 * Simulate retry taking longer than 5 minutes.
	 * 
	 * @throws ParseException
	 * @throws FailedToStopException 
	 */
	public void testLongRetry() throws ParseException, FailedToStopException {
		
		FlagState job = new FlagState();
		job.setState(JobState.INCOMPLETE);
		
		TimeSchedule time = new TimeSchedule();
		time.setFrom("14:45");
		time.setTo("15:00");

		Interval limits = time.nextDue(
				new ScheduleContext(
						DateHelper.parseDateTime("2009-02-10 12:00")));
		
		IntervalSchedule retry = new IntervalSchedule();
		retry.setInterval("00:05");
		
		OurClock clock = new OurClock();
		clock.date = DateHelper.parseDateTime("2009-02-10 14:50");

		Retry test = new Retry();
		test.setLimits(limits);
		test.setSchedule(retry);
		test.setClock(clock);
		test.setJob(job);
		
		OurOddjobServices oddjobServices = new OurOddjobServices();
		
		test.setScheduleExecutorService(
				oddjobServices.getScheduledExecutor());
		
		test.run();
		
		assertNotNull(oddjobServices.runnable);
		
		assertEquals(0, oddjobServices.delay);		
		assertEquals(
				DateHelper.parseDateTime("2009-02-10 14:50"),
				test.getNextDue());

		oddjobServices.delay = -1;
		clock.date = DateHelper.parseDateTime("2009-02-10 14:57");
		
		oddjobServices.runnable.run();

		assertEquals(ParentState.STARTED, test.lastStateEvent().getState());
		
		assertEquals(0, oddjobServices.delay);
		assertEquals(
				DateHelper.parseDateTime("2009-02-10 14:55"),
				test.getNextDue());
		
		oddjobServices.delay = -1;
		
		oddjobServices.runnable.run();
		
		assertEquals(-1, oddjobServices.delay);
		assertEquals(null,
				test.getNextDue());
		
		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());
		
		test.stop();
		
		assertFalse(oddjobServices.canceled);
	}

	/**
	 * Test A Manual Re-run to complete cancels the retry.
	 * 
	 * @throws ParseException
	 * @throws FailedToStopException 
	 */
	public void testManualRetry() throws ParseException, FailedToStopException {
		
		FlagState job = new FlagState();
		job.setState(JobState.INCOMPLETE);

		IntervalSchedule schedule = new IntervalSchedule();
		schedule.setInterval("12:00");
		
		OurClock clock = new OurClock();
		clock.date = DateHelper.parseDateTime("2009-04-03 08:00");

		Retry test = new Retry();
		test.setSchedule(schedule);
		test.setClock(clock);
		test.setJob(job);
		
		OurOddjobServices oddjobServices = new OurOddjobServices();
		
		test.setScheduleExecutorService(
				oddjobServices.getScheduledExecutor());
		
		test.run();
		
		oddjobServices.runnable.run();
		
		assertEquals(12 * 60 * 60 * 1000, oddjobServices.delay);
		
		job.setState(JobState.COMPLETE);
		job.softReset();
		job.run();

		// No affect.
		assertEquals(12 * 60 * 60 * 1000, oddjobServices.delay);
		assertEquals(
				DateHelper.parseDateTime("2009-04-03 20:00"),
				test.getNextDue());

		oddjobServices.delay = -1;
		
		// next time timer runs job it should go straight to complete.
		oddjobServices.runnable.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());		
		assertEquals(-1, oddjobServices.delay);
		
		test.stop();
		
		assertFalse(oddjobServices.canceled);
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());		
	}
	
	private class OurJob extends MockStateful
	implements Runnable, Resetable {

		boolean reset;
		
		final List<StateListener> listeners = new ArrayList<StateListener>();
		
		public void addStateListener(StateListener listener) {
			listeners.add(listener);
			listener.jobStateChange(new StateEvent(this, JobState.READY));
			
		}
		public void removeStateListener(StateListener listener) {
			listeners.remove(listener);
		}

		
		public void run() {
			List<StateListener> copy = new ArrayList<StateListener>(listeners);
			for (StateListener listener: copy) {
				listener.jobStateChange(new StateEvent(this, JobState.COMPLETE));
			}
		}
		
		public boolean hardReset() {
			throw new RuntimeException("Unexpected.");
		}
		
		public boolean softReset() {
			reset = true;
			return true;
		}
	}

	/**
	 * Test a one off schedule that completes.
	 * 
	 * @throws Exception
	 */
	public void testSimpleSchedule() 
	throws Exception {
		DateSchedule schedule = new DateSchedule();
		schedule.setOn("2020-12-25");
		
		OurJob ourJob = new OurJob();
				
		Retry test = new Retry();
		test.setSchedule(schedule);
		test.setJob(ourJob);
		
		OurOddjobServices oddjobServices = new OurOddjobServices();		
		test.setScheduleExecutorService(
				oddjobServices.getScheduledExecutor());

		test.run();
		
		Date expected = DateHelper.parseDate("2020-12-25");
		
		assertEquals(expected, test.getNextDue());
		assertEquals(expected, test.getCurrent().getFromDate());

		oddjobServices.delay = -1;
		
		oddjobServices.runnable.run();
		
		assertNull(null, test.getNextDue());	
		assertEquals(-1, oddjobServices.delay);

		assertTrue(ourJob.reset);
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());

		test.setJob(null);
		
		test.destroy();
		
		assertEquals(0, ourJob.listeners.size());

		assertFalse(oddjobServices.canceled);
	}
	
	/**
	 * Schedule doesn't have to be serializable.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void testSerializeUnserializbleSchedule() throws IOException, ClassNotFoundException {

		Retry test = new Retry();
		test.setSchedule(new Schedule() {
			public IntervalTo nextDue(ScheduleContext context) {
				return null;
			}
		});
		
		Retry copy = OddjobTestHelper.copy(test);
		
		assertNull(copy.getSchedule());
	}

	public void testStateNotifications() throws InterruptedException {
		
		FlagState incomplete = new FlagState(JobState.INCOMPLETE);

		DefaultExecutors defaultServices = new DefaultExecutors();
		
		Retry test = new Retry();
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.STARTED, ParentState.INCOMPLETE);
				
		CountSchedule count = new CountSchedule();
		count.setCount(3);
		count.setRefinement(new NowSchedule());
		test.setSchedule(count);
		
		test.setScheduleExecutorService(
				defaultServices.getScheduledExecutor());

		SequenceJob sequence = new SequenceJob();
		sequence.setFrom(1);
		
		Resets resets = new Resets();
		resets.setHarden(true);
		resets.setJob(sequence);
		
		ParallelJob parallel = new ParallelJob();
		parallel.setExecutorService(defaultServices.getPoolExecutor());
		parallel.setJobs(0, resets);
		parallel.setJobs(1, incomplete);
		
		test.setJob(parallel);
		
		test.run();

		steps.checkWait();
		
		assertEquals(3, sequence.getCurrent().intValue());
		
		steps.startCheck(ParentState.INCOMPLETE, ParentState.READY);
		
		test.softReset();
		
		steps.checkNow();
		
		steps.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.STARTED, ParentState.INCOMPLETE);
		
		test.run();
		
		steps.checkWait();
		
		assertEquals(6, sequence.getCurrent().intValue());
		
		defaultServices.stop();
	}

	private class ExecuteImmediately 
	extends MockScheduledExecutorService {
				
		public ScheduledFuture<?> schedule(Runnable runnable, long delay,
				TimeUnit unit) {
			new Thread(runnable).start();
			return new MockScheduledFuture<Void>();
		}
	};
	
	private class LockExamineJob extends SimpleJob {
		int i = 0;
		
		@Override
		protected int execute() throws Throwable {
			if (i++ == 2) {
				return 0;
			}
			return 1;
		}
		
		@Override
		public boolean hardReset() {
			throw new RuntimeException("Unexpected.");
		}
		
		@Override
		public boolean softReset() {
			try {
				stateHandler.assertLockHeld();
				fail("Shouldn't be locked.");
			}
			catch (IllegalStateException e) {
				/// expected.
			}
			
			return super.softReset();
		}
		
	}
	
	public void testLocking() throws FailedToStopException {
		
		Retry test = new Retry();
		test.setScheduleExecutorService(
				new ExecuteImmediately());
		test.setSchedule(new NowSchedule());
		test.setJob(new LockExamineJob());
		
		test.run();
		
		new StopWait(test).run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
	}
	
	private class TestListeners extends SimpleJob {
		int i = 0;
		int listeners = 0;
		@Override
		protected int execute() throws Throwable {
			assertEquals(2, listeners);
			
			if (i++ == 2) {
				return 0;
			}
			return 1;
		}
		
		@Override
		public boolean hardReset() {
			throw new RuntimeException("Unexpected.");
		}
		
		@Override
		public boolean softReset() {
			return super.softReset();
		}

		@Override
		public void addStateListener(StateListener listener) {
			listeners++;
			super.addStateListener(listener);
		}
		
		@Override
		public void removeStateListener(StateListener listener) {
			listeners--;
			super.removeStateListener(listener);
		}
	}
	
	/** Test for Bug where listener wasn't removed before scheduling. 
	 * @throws FailedToStopException */
	public void testListeners() throws FailedToStopException {
		
		Retry test = new Retry();
		test.setScheduleExecutorService(new ExecuteImmediately());
		test.setSchedule(new NowSchedule());
		test.setJob(new TestListeners());
		
		test.run();
		
		new StopWait(test).run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
	}
	
	private class ExecuteNever 
	extends MockScheduledExecutorService {
				
		public ScheduledFuture<?> schedule(Runnable runnable, long delay,
				TimeUnit unit) {
			return new MockScheduledFuture<Void>() {
				@Override
				public boolean cancel(boolean mayInterruptIfRunning) {
					return true;
				}
				
			};
		}
	};
	
	private class LaterJob extends SimpleJob {
		
		void start() {
			stateHandler.waitToWhen(StateConditions.READY, new Runnable() {
				public void run() {
					stateHandler.setState(JobState.EXECUTING);
					stateHandler.fireEvent();
				}
			});
		}
		
		void complete() {
			stateHandler.waitToWhen(StateConditions.EXECUTING, new Runnable() {
				public void run() {
					stateHandler.setState(JobState.COMPLETE);
					stateHandler.fireEvent();
				}
			});
		}
		
		@Override
		protected int execute() throws Throwable {
			throw new RuntimeException("Unexpected.");
		}
		
		@Override
		public boolean hardReset() {
			throw new RuntimeException("Unexpected.");
		}
		
		@Override
		public boolean softReset() {
			throw new RuntimeException("Unexpected.");
		}
		
	}
	
	public void testStopFirst() throws FailedToStopException {
		
		Retry test = new Retry();
		test.setScheduleExecutorService(new ExecuteNever());
		test.setSchedule(new NowSchedule()); // schedule is irrelevant
		
		final LaterJob job = new LaterJob();
		test.setJob(job);
		
		test.run();
		
		assertEquals(ParentState.STARTED, test.lastStateEvent().getState());

		job.start();
		
		assertEquals(ParentState.STARTED, test.lastStateEvent().getState());
		
		SimpleJob stop = new SimpleJob() {
			@Override
			protected int execute() throws Throwable {
				job.complete();				
				return 0;
			}
		};
		new Thread(stop).start();
		
		test.stop();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
	}
	
	public void testStopLast() throws FailedToStopException {
		
		Retry test = new Retry();
		test.setScheduleExecutorService(new ExecuteNever());
		test.setSchedule(new NowSchedule()); // schedule is irrelevant
		
		LaterJob job = new LaterJob(); 
		test.setJob(job);
		
		test.run();
		
		assertEquals(ParentState.STARTED, test.lastStateEvent().getState());

		job.start();
		job.complete();
		
		assertEquals(ParentState.STARTED, test.lastStateEvent().getState());
		
		test.stop();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
	}
	
	public void testWithLimitsLongOverDue() throws ParseException {
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		OurClock clock = new OurClock("2010-07-25 08:00");
		
		IntervalTo limits = new IntervalTo(
				DateHelper.parseDateTime("2010-06-10 07:00"), 
				DateHelper.parseDateTime("2010-06-10 10:00") );
		
		OurOddjobServices services = new OurOddjobServices();
		
		Schedule schedule = new IntervalSchedule(15 * 60 * 1000L);
		
		Retry test = new Retry();
		test.setClock(clock);
		test.setLimits(limits);
		test.setSchedule(schedule);
		test.setScheduleExecutorService(services.getScheduledExecutor());
		test.setJob(job);
		test.run();
		
		assertNotNull(services.runnable);
		assertEquals(0, services.delay);
		assertEquals(DateHelper.parseDateTime("2010-06-10 07:00"), 
				test.getNextDue());
		
		services.runnable.run();
		
		assertEquals(null, test.getNextDue());
	}
	
	public void testWithLimitsLongOverDueCountSchedule() throws ParseException {
		
		FlagState job = new FlagState(JobState.INCOMPLETE);
		
		OurClock clock = new OurClock("2010-07-25 08:00");
		
		IntervalTo limits = new IntervalTo(
				DateHelper.parseDateTime("2010-06-10 07:00"), 
				DateHelper.parseDateTime("2010-06-10 10:00") );
		
		OurOddjobServices services = new OurOddjobServices();
		
		CountSchedule schedule = new CountSchedule(3);
		schedule.setRefinement(new NowSchedule());
		
		Retry test = new Retry();
		test.setClock(clock);
		test.setLimits(limits);
		test.setSchedule(schedule);
		test.setScheduleExecutorService(services.getScheduledExecutor());
		test.setJob(job);
		test.run();
		
		assertNotNull(services.runnable);
		assertEquals(0, services.delay);
		assertEquals(DateHelper.parseDateTime("2010-06-10 07:00"), 
				test.getNextDue());
		
		services.runnable.run();
		
		assertEquals(null, test.getNextDue());
	}
	
	public void testFilePollingExample() throws FailedToStopException, InterruptedException {
		
	    	Oddjob oddjob = new Oddjob();
	    	oddjob.setConfiguration(new XMLConfiguration(
	    			"org/oddjob/scheduling/RetryExample.xml",
	    			getClass().getClassLoader()));
	    	
	    	
	    	oddjob.load();
	    	
	    	OddjobLookup lookup = new OddjobLookup(oddjob);
	    	
	    	Stateful exists = (Stateful) lookup.lookup("check");
	    	
	    	StateSteps oddjobStates = new StateSteps(oddjob);
	    	oddjobStates.startCheck(ParentState.READY, 
	    			ParentState.EXECUTING, 
	    			ParentState.STARTED);
	    	
	    	StateSteps existsStates = new StateSteps(exists);
	    	existsStates.startCheck(JobState.READY, 
	    			JobState.EXECUTING, 
	    			JobState.INCOMPLETE);
	    		    	
	    	oddjob.run();
	    	
	    	oddjobStates.checkNow();
	    	existsStates.checkWait();
	    	
	    	oddjobStates.startCheck(ParentState.STARTED, ParentState.READY);
	    	
	    	oddjob.stop();
	    	
	    	oddjobStates.checkNow();
	    	
	    	oddjob.destroy();
	}	    	
}
