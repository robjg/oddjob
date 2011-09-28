/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.MockStateful;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.Resetable;
import org.oddjob.StateSteps;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.jobs.WaitJob;
import org.oddjob.jobs.job.StopJob;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.schedules.CountSchedule;
import org.oddjob.schedules.schedules.DailySchedule;
import org.oddjob.schedules.schedules.DateSchedule;
import org.oddjob.schedules.schedules.IntervalSchedule;
import org.oddjob.schedules.schedules.NowSchedule;
import org.oddjob.schedules.schedules.TimeSchedule;
import org.oddjob.state.FlagState;
import org.oddjob.state.IsNot;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.util.Clock;

/**
 * 
 */
public class TimerTest extends TestCase {
	private static final Logger logger = Logger.getLogger(TimerTest.class);
	
	protected void setUp() {
		logger.debug("=============== " + getName() + " ===================");
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
			reset = true;
			return true;
		}
		
		public boolean softReset() {
			throw new RuntimeException("Unexpected.");
		}
	}
	
	private class OurOddjobServices extends MockScheduledExecutorService {
				
		Runnable runnable;
		long delay;

		public ScheduledFuture<?> schedule(Runnable runnable, long delay,
				TimeUnit unit) {

			OurOddjobServices.this.delay = delay;
			OurOddjobServices.this.runnable = runnable;

			return new MockScheduledFuture<Void>();
		}
	};
	
	public void testSimpleSchedule() 
	throws Exception {
		DateSchedule schedule = new DateSchedule();
		schedule.setOn("2020-12-25");
		
		OurJob ourJob = new OurJob();
				
		Timer test = new Timer();
		test.setSchedule(schedule);
		test.setJob(ourJob);
		test.setHaltOnFailure(true);
		
		OurOddjobServices oddjobServices = new OurOddjobServices();		
		test.setScheduleExecutorService(oddjobServices);

		test.run();
		
		assertEquals(ParentState.ACTIVE, 
				test.lastStateEvent().getState());
		
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

	}

	private class OurClock implements Clock {

		Date date;
		public Date getDate() {
			
			return date;
		}
	}
	
	public void testAnotherSchedule() throws ParseException {
	
		FlagState job = new FlagState();
		job.setState(JobState.COMPLETE);
		
		DailySchedule time = new DailySchedule();
		time.setFrom("14:45");
		time.setTo("14:55");
		
		OurClock clock = new OurClock();
		clock.date = DateHelper.parseDateTime("2009-02-10 14:50");

		
		Timer test = new Timer();
		test.setSchedule(time);
		test.setClock(clock);
		test.setJob(job);
		
		OurOddjobServices oddjobServices = new OurOddjobServices();
		
		test.setScheduleExecutorService(oddjobServices);
		
		test.run();
		
		assertNotNull(oddjobServices.runnable);
		assertEquals(0, oddjobServices.delay);
		
		oddjobServices.runnable.run();

		Date expectedNextDue = DateHelper.parseDateTime(
				"2009-02-11 14:45");
		
		assertEquals(expectedNextDue, test.getNextDue());
		
		assertEquals(expectedNextDue.getTime() -clock.date.getTime(),
				oddjobServices.delay);
	}
	
	public void testOverdueSchedule() throws ParseException {
		
		FlagState job = new FlagState();
		job.setState(JobState.COMPLETE);
		
		DailySchedule time = new DailySchedule();
		time.setAt("12:00");
		
		OurClock clock = new OurClock();
		clock.date = DateHelper.parseDateTime("2009-03-02 14:00");

		Timer test = new Timer();
		test.setSchedule(time);
		test.setClock(clock);
		test.setJob(job);
		
		OurOddjobServices oddjobServices = new OurOddjobServices();
		
		test.setScheduleExecutorService(oddjobServices);
		
		test.run();
		
		assertNotNull(oddjobServices.runnable);
		assertEquals(22 * 60 * 60 * 1000, oddjobServices.delay);
		
		// simulate job longer than next due;
		clock.date = DateHelper.parseDateTime("2009-03-04 13:00");
		oddjobServices.runnable.run();

		assertEquals(0, oddjobServices.delay);
		
		// next one runs quick.
		clock.date = DateHelper.parseDateTime("2009-03-04 18:00");
		oddjobServices.runnable.run();

		assertEquals(18 * 60 * 60 * 1000, oddjobServices.delay);
	}
	
	
	public void testSkipMissedSchedule() throws ParseException {
		
		FlagState job = new FlagState();
		job.setState(JobState.COMPLETE);
		
		DailySchedule time = new DailySchedule();
		time.setAt("12:00");
		
		OurClock clock = new OurClock();
		clock.date = DateHelper.parseDateTime("2009-03-02 14:00");

		Timer test = new Timer();
		test.setSchedule(time);
		test.setClock(clock);
		test.setJob(job);
		test.setSkipMissedRuns(true);
		
		OurOddjobServices oddjobServices = new OurOddjobServices();
		
		test.setScheduleExecutorService(oddjobServices);
		
		test.run();
		
		assertNotNull(oddjobServices.runnable);
		assertEquals(22 * 60 * 60 * 1000, oddjobServices.delay);
		
		// simulate job longer than next due;
		clock.date = DateHelper.parseDateTime("2009-03-04 13:00");
		oddjobServices.runnable.run();

		// next one runs the next day.
		assertEquals(23 * 60 * 60 * 1000, oddjobServices.delay);
		
	}
	
	public void testHaltOnFailure() throws ParseException {
		
		FlagState job = new FlagState();
		job.setState(JobState.INCOMPLETE);
		
		TimeSchedule time = new TimeSchedule();
		time.setFrom("14:45");
		time.setTo("14:55");
		
		OurClock clock = new OurClock();
		clock.date = DateHelper.parseDateTime("2009-02-10 14:50");

		Timer test = new Timer();
		test.setSchedule(time);
		test.setClock(clock);
		test.setHaltOnFailure(true);
		test.setJob(job);
		
		OurOddjobServices oddjobServices = new OurOddjobServices();
		
		test.setScheduleExecutorService(oddjobServices);
		
		test.run();
		
		assertNotNull(oddjobServices.runnable);
		assertEquals(0, oddjobServices.delay);
		
		oddjobServices.delay = -1;
		
		oddjobServices.runnable.run();
		
		assertEquals(-1, oddjobServices.delay);
		assertEquals(null, test.getNextDue());

		assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());		
	}
	
	public void testTimeZone() 
	throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		
		DateSchedule schedule = new DateSchedule();
		schedule.setOn("2020-06-21");
		DailySchedule daily = new DailySchedule();
		daily.setAt("10:00");
		schedule.setRefinement(daily);
		
		OurJob ourJob = new OurJob();
		
		Timer test = new Timer();
		test.setSchedule(schedule);
		test.setJob(ourJob);
		test.setTimeZone("GMT+8");
		
		OurOddjobServices oddjobServices = new OurOddjobServices();
		
		test.setScheduleExecutorService(oddjobServices);
		
		test.run();
		
		assertEquals(DateHelper.parseDateTime("2020-06-21 02:00"),
				test.getNextDue());	
		
		TimeZone.setDefault(null);
	}
	
	public void testSerialize() throws Exception {
		
		FlagState sample = new FlagState();
		sample.setState(JobState.COMPLETE);

		Timer test = new Timer();
		
		IntervalSchedule interval = new IntervalSchedule();
		interval.setInterval("00:00:05");
		CountSchedule count = new CountSchedule();
		count.setCount(2);
		count.setRefinement(interval);
		
		OurClock clock = new OurClock();
		clock.date = DateHelper.parseDateTime("2009-02-10 14:30");
		
		test.setSchedule(count);
		test.setJob(sample);
		test.setClock(clock);
		
		OurOddjobServices oddjobServices = new OurOddjobServices();
		
		test.setScheduleExecutorService(oddjobServices);

		test.run();
		
		assertEquals(0, oddjobServices.delay);
		
		oddjobServices.runnable.run();
		
		assertEquals(5000, oddjobServices.delay);
		
		Timer copy = (Timer) Helper.copy(test);

		copy.setClock(clock);
		copy.setScheduleExecutorService(oddjobServices);
		
		assertEquals(5000, oddjobServices.delay);

		Runnable runnable = oddjobServices.runnable; 
		
		oddjobServices.runnable = null;
		
		runnable.run();
		
		assertNull(copy.getNextDue());
		assertNull(oddjobServices.runnable);
		
	}
	
	public void testSerializeNotComplete() throws Exception {
		
		FlagState sample = new FlagState();
		sample.setState(JobState.INCOMPLETE);

		Timer test = new Timer();
		
		OurClock clock = new OurClock();
		clock.date = DateHelper.parseDateTime("2009-02-10 14:30");
		
		test.setSchedule(new NowSchedule());
		test.setJob(sample);
		test.setClock(clock);
		
		OurOddjobServices oddjobServices = new OurOddjobServices();
		
		test.setScheduleExecutorService(oddjobServices);

		test.run();
		
		assertEquals(0, oddjobServices.delay);
		
		oddjobServices.runnable.run();

		Timer copy = (Timer) Helper.copy(test);

		assertEquals(ParentState.READY, copy.lastStateEvent().getState());
		assertEquals(null, test.getLastComplete());
		
	}
	
	private class OurStopServices extends MockScheduledExecutorService {
		
		public ScheduledFuture<?> schedule(Runnable runnable, long delay,
				TimeUnit unit) {

			if (delay < 1) {
				new Thread(runnable).start();
			}

			return new MockScheduledFuture<Void>() {
				public boolean cancel(boolean interrupt) {
					return false;
				}
			};
		}
	};
	
	public void testStop() throws ParseException {
		
		final Timer test = new Timer();
		test.setSchedule(new NowSchedule());
		
		IntervalSchedule interval = new IntervalSchedule();
		interval.setInterval("00:15");
		
		StopJob stop = new StopJob();
		stop.setExecutorService(new MockExecutorService() {
			@Override
			public Future<?> submit(Runnable task) {
				new Thread(task).start();
				return null;
			}
		});
		stop.setAsync(true);
		stop.setJob(test);
		
		Retry retry = new Retry();
		retry.setSchedule(interval);
		
		retry.setJob(stop);

		test.setJob(retry);
		
		OurStopServices services = new OurStopServices();
		
		test.setScheduleExecutorService(services);
		retry.setScheduleExecutorService(services);
		
		test.run();
		
		WaitJob wait = new WaitJob();
		wait.setFor(test);
		wait.setState(new IsNot(StateConditions.EXECUTING));
		wait.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		retry.destroy();
		test.destroy();
		
	}

	public void testStopBeforeTriggered() throws FailedToStopException {
		
		Timer test = new Timer();
		test.setScheduleExecutorService(new MockScheduledExecutorService() {
			@Override
			public ScheduledFuture<?> schedule(Runnable command, long delay,
					TimeUnit unit) {
				return null;
			}
		});
		test.setJob(new SimpleJob() {
			@Override
			protected int execute() throws Throwable {
				throw new Exception();
			}
		});
		test.setSchedule(new NowSchedule());
		
		test.run();
		
		assertEquals(ParentState.ACTIVE, test.lastStateEvent().getState());
		
		test.stop();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
	}
	
	/**
	 * Schedule doesn't have to be serializable.

	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void testSerializeUnserializbleSchedule() throws IOException, ClassNotFoundException {

		Timer test = new Timer();
		test.setSchedule(new Schedule() {
			public IntervalTo nextDue(ScheduleContext context) {
				return null;
			}
		});
		
		Timer copy = Helper.copy(test);
		
		assertNull(copy.getSchedule());
	}
	
	public void testPersistedScheduleInOddjob() throws FailedToStopException, ArooaPropertyException, ArooaConversionException, InterruptedException, IOException, ParseException {
		
		OurDirs dirs = new OurDirs();
		
		File persistDir = dirs.relative("work/persisted-schedule");
		if (persistDir.exists()) {
			FileUtils.forceDelete(persistDir);
		}
	
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(dirs.relative("test/conf/persisted-schedule.xml"));
		
		oddjob.setExport("clock", new ArooaObject(
				new ManualClock("2011-03-09 06:30")));
		oddjob.setExport("work-dir", new ArooaObject( 
				dirs.relative("work")));
		
		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, 
				oddjob.lastStateEvent().getState());
		
		oddjob.stop();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		String text = lookup.lookup("persisted-schedule/scheduled-job.text", 
				String.class);
		
		assertEquals(null, text);
		
		Date nextDue = lookup.lookup("persisted-schedule/schedule1.nextDue",
				Date.class);
		
		assertEquals(DateHelper.parseDateTime("2011-03-10 05:30"), nextDue);
		
		oddjob.setExport("clock", new ArooaObject(
				new ManualClock("2011-03-10 07:00")));
		
		oddjob.hardReset();
		oddjob.run();
		
		while (true) {
			text = lookup.lookup("persisted-schedule/scheduled-job.text", 
					String.class);
			if (text != null) {
				break;
			}
			logger.info("Waiting for scheduled job to run.");
			
			Thread.sleep(500);
		}

		assertEquals("Job schedule at 2011-03-10 05:30:00.000 " +
				"but running at 2011-03-10 07:00:00.000", 
				text);
		
		oddjob.stop();
		
		oddjob.destroy();
	}
	
	public void testTimerExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {
		
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/scheduling/TimerExample.xml",
    			getClass().getClassLoader()));
    	
    	oddjob.load();
    	
    	assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
    	
    	OddjobLookup lookup = new OddjobLookup(oddjob);
    	
    	Timer timer = lookup.lookup("timer", Timer.class);
    	
    	timer.setClock(new ManualClock("2011-04-08 09:59:59.750"));
    	
    	oddjob.run();
    	
    	assertEquals(ParentState.ACTIVE, oddjob.lastStateEvent().getState());
    	
    	String result = null;
    	
    	while (true) {
    		result = lookup.lookup("work.text", String.class);
    		if (result != null) {
    			break;
    		}
    		
			logger.info("Waiting for scheduled job to run.");
			
    		Thread.sleep(500);
    	}
    	
    	assertEquals("Doing some work at 2011-04-08 10:00:00.000",
    			result);
    	
    	oddjob.stop();
    	
    	assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
    	
    	oddjob.destroy();
    	
	}
	
	public void testTimerOnceExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {
		
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/scheduling/TimerOnceExample.xml",
    			getClass().getClassLoader()));
    	
    	oddjob.load();
    	
    	assertEquals(ParentState.READY, 
    			oddjob.lastStateEvent().getState());
    	
    	Timer timer = (Timer) new OddjobLookup(oddjob).lookup("timer");
    	timer.setClock(new ManualClock("2011-09-28 09:59:59.999"));
    	
    	StateSteps states = new StateSteps(oddjob);
    	states.startCheck(ParentState.READY, 
    			ParentState.EXECUTING, 
    			ParentState.ACTIVE, 
    			ParentState.COMPLETE);
    	
    	oddjob.run();
    	
    	states.checkWait();
    	    	
    	oddjob.destroy();
	}
}
