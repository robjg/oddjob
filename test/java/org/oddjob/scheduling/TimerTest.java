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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.IconSteps;
import org.oddjob.MockOddjobExecutors;
import org.oddjob.MockStateful;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.Resetable;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.images.IconHelper;
import org.oddjob.jobs.WaitJob;
import org.oddjob.persist.MapPersister;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.SimpleInterval;
import org.oddjob.schedules.SimpleScheduleResult;
import org.oddjob.schedules.schedules.CountSchedule;
import org.oddjob.schedules.schedules.DailySchedule;
import org.oddjob.schedules.schedules.DateSchedule;
import org.oddjob.schedules.schedules.IntervalSchedule;
import org.oddjob.schedules.schedules.NowSchedule;
import org.oddjob.schedules.schedules.TimeSchedule;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

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

		int resets;
		
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
				listener.jobStateChange(new StateEvent(this, JobState.EXECUTING));
				listener.jobStateChange(new StateEvent(this, JobState.COMPLETE));
			}
		}
		
		public boolean hardReset() {
			++resets;
			return true;
		}
		
		public boolean softReset() {
			throw new RuntimeException("Unexpected.");
		}
	}
	
	private class OurScheduledExecutorService extends MockScheduledExecutorService {
				
		Runnable runnable;
		long delay;

		ScheduledFuture<Void> future = new MockScheduledFuture<Void>();
		
		public ScheduledFuture<?> schedule(Runnable runnable, long delay,
				TimeUnit unit) {

			OurScheduledExecutorService.this.delay = delay;
			OurScheduledExecutorService.this.runnable = runnable;

			return future;
		}
	};
	
	public void testSimpleNonRepeatingSchedule() 
	throws Exception {
		
		DateSchedule schedule = new DateSchedule();
		schedule.setOn("2020-12-25");
		
		OurJob ourJob = new OurJob();

		ManualClock clock = new ManualClock("2020-12-24");
		
		Timer test = new Timer();
		test.setSchedule(schedule);
		test.setJob(ourJob);
		test.setHaltOnFailure(true);
		test.setClock(clock);
		
		OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();
		test.setScheduleExecutorService(oddjobServices);

		StateSteps timerStates = new StateSteps(test);
		timerStates.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.STARTED);
		IconSteps timerIcons = new IconSteps(test);
		timerIcons.startCheck(IconHelper.READY, IconHelper.EXECUTING, 
				IconHelper.SLEEPING);
		
		test.run();
		
		timerStates.checkNow();
		timerIcons.checkNow();
				
		Date expected = DateHelper.parseDate("2020-12-25");
		
		assertEquals(expected, test.getNextDue());
		assertEquals(expected, test.getCurrent().getFromDate());
		assertEquals(24 * 60 * 60 * 1000L, oddjobServices.delay);

		timerStates.startCheck(ParentState.STARTED,
				ParentState.COMPLETE);
		timerIcons.startCheck(IconHelper.SLEEPING, IconHelper.STARTED, 
				IconHelper.COMPLETE);
		
		// Time passes... Executor runs job.
		oddjobServices.delay = -1;		
		oddjobServices.runnable.run();
		oddjobServices.runnable = null;
		
		assertNull(null, test.getNextDue());	
		assertNull(null, test.getCurrent());	
		assertEquals(expected, test.getLastDue());	
		assertEquals(-1, oddjobServices.delay);

		assertEquals(1, ourJob.resets);
		timerStates.checkNow();
		timerIcons.checkNow();
		
		//
		// Check reset and run again works as expected.
		
		clock.setDateText("2020-12-25 00:00:01");
		
		timerStates.startCheck(ParentState.COMPLETE, ParentState.READY);
		timerIcons.startCheck(IconHelper.COMPLETE, IconHelper.READY);
		
		test.hardReset();
		
		timerStates.checkNow();
		timerIcons.checkNow();
		
		timerStates.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.STARTED);
		timerIcons.startCheck(IconHelper.READY, IconHelper.EXECUTING, 
				IconHelper.SLEEPING);
		
		test.run();
		
		timerStates.checkNow();
		timerIcons.checkNow();
		
		assertEquals(expected, test.getNextDue());
		assertEquals(expected, test.getCurrent().getFromDate());
		assertEquals(0L, oddjobServices.delay);

		timerStates.startCheck(ParentState.STARTED, 
				ParentState.COMPLETE);
		timerIcons.startCheck(IconHelper.SLEEPING, IconHelper.STARTED, 
				IconHelper.COMPLETE);
		
		// Time has passed... Executor would run job immediately.
		oddjobServices.delay = -1;		
		oddjobServices.runnable.run();
		oddjobServices.runnable = null;
		
		assertNull(null, test.getNextDue());	
		assertNull(null, test.getCurrent());	
		assertEquals(expected, test.getLastDue());	
		assertEquals(-1, oddjobServices.delay);

		assertEquals(3, ourJob.resets);
		timerStates.checkNow();
		timerIcons.checkNow();
		
		
		//
		// Destroy
		test.setJob(null);
		
		test.destroy();
		
		assertEquals(0, ourJob.listeners.size());
	}

	public void testRecurringScheduleWhenStopped() throws ParseException {
	
		FlagState job = new FlagState();
		job.setState(JobState.COMPLETE);
		
		DailySchedule time = new DailySchedule();
		time.setFrom("14:45");
		time.setTo("14:55");
		
		ManualClock clock = new ManualClock("2009-02-10 14:50");
		
		Timer test = new Timer();
		test.setSchedule(time);
		test.setClock(clock);
		test.setJob(job);
		
		OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();
		
		test.setScheduleExecutorService(oddjobServices);
		
		test.run();
		
		assertNotNull(oddjobServices.runnable);
		assertEquals(0, oddjobServices.delay);
		
		oddjobServices.runnable.run();

		Date expectedNextDue = DateHelper.parseDateTime(
				"2009-02-11 14:45");
		
		assertEquals(expectedNextDue, test.getNextDue());
		
		assertEquals(expectedNextDue.getTime() -clock.getDate().getTime(),
				oddjobServices.delay);
	}
	
	public void testOverdueSchedule() throws ParseException {
		
		FlagState job = new FlagState();
		job.setState(JobState.COMPLETE);
		
		DailySchedule time = new DailySchedule();
		time.setAt("12:00");
		
		ManualClock clock = new ManualClock("2009-03-02 14:00");

		Timer test = new Timer();
		test.setSchedule(time);
		test.setClock(clock);
		test.setJob(job);
		
		OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();
		
		test.setScheduleExecutorService(oddjobServices);
		
		test.run();
		
		assertNotNull(oddjobServices.runnable);
		assertEquals(22 * 60 * 60 * 1000, oddjobServices.delay);
		
		// simulate job longer than next due;
		clock.setDateText("2009-03-04 13:00");
		oddjobServices.runnable.run();

		assertEquals(0, oddjobServices.delay);
		
		// next one runs quick.
		clock.setDateText("2009-03-04 18:00");
		oddjobServices.runnable.run();

		assertEquals(18 * 60 * 60 * 1000, oddjobServices.delay);
	}
	
	public void testSkipMissedSchedule() throws ParseException {
		
		FlagState job = new FlagState();
		job.setState(JobState.COMPLETE);
		
		DailySchedule time = new DailySchedule();
		time.setAt("12:00");
		
		ManualClock clock = new ManualClock("2009-03-02 14:00");

		Timer test = new Timer();
		test.setSchedule(time);
		test.setClock(clock);
		test.setJob(job);
		test.setSkipMissedRuns(true);
		
		OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();
		
		test.setScheduleExecutorService(oddjobServices);
		
		test.run();
		
		assertNotNull(oddjobServices.runnable);
		assertEquals(22 * 60 * 60 * 1000, oddjobServices.delay);
		
		// simulate job longer than next due;
		clock.setDateText("2009-03-04 13:00");
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
		
		ManualClock clock = new ManualClock("2009-02-10 14:50");

		Timer test = new Timer();
		test.setSchedule(time);
		test.setClock(clock);
		test.setHaltOnFailure(true);
		test.setJob(job);
		
		OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();
		
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
		
		OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();
		
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
		
		ManualClock clock = new ManualClock("2009-02-10 14:30");
		
		test.setSchedule(count);
		test.setJob(sample);
		test.setClock(clock);
		
		OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();
		
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
		
		ManualClock clock = new ManualClock("2009-02-10 14:30");
		
		test.setSchedule(new NowSchedule());
		test.setJob(sample);
		test.setClock(clock);
		
		OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();
		
		test.setScheduleExecutorService(oddjobServices);

		test.run();
		
		assertEquals(0, oddjobServices.delay);
		
		oddjobServices.runnable.run();

		Timer copy = (Timer) Helper.copy(test);

		assertEquals(ParentState.READY, copy.lastStateEvent().getState());
		assertEquals(DateHelper.parseDateTime("2009-02-10 14:30"), 
				test.getLastDue());
		assertEquals(new SimpleScheduleResult(
				new SimpleInterval(
						DateHelper.parseDateTime("2009-02-10 14:30:00.001"))), 
				test.getCurrent());
		
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
	
	public void testStop() throws ParseException, InterruptedException, FailedToStopException {
		
		final Timer test = new Timer();
		test.setSchedule(new CountSchedule(2));
		
		IntervalSchedule interval = new IntervalSchedule();
		interval.setInterval("00:15");
		
		final IconSteps checkFirstThreadFinished = new IconSteps(test);
		checkFirstThreadFinished.startCheck(IconHelper.READY, 
				IconHelper.EXECUTING, IconHelper.SLEEPING, 
				IconHelper.STARTED, IconHelper.SLEEPING);
		
		Retry retry = new Retry();
		retry.setSchedule(interval);
		
		SimpleJob child = new SimpleJob() {
			int i;
			Runnable[] jobs = {
				new FlagState(), new WaitJob()	
			};
			@Override
			protected int execute() throws Throwable {
				jobs[i++].run();
				return 0;
			}
		};
		
		retry.setJob(child);

		test.setJob(retry);
		
		OurStopServices services = new OurStopServices();
		
		test.setScheduleExecutorService(services);
		retry.setScheduleExecutorService(services);
		
		StateSteps timerStates = new StateSteps(test);
		timerStates.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.STARTED, ParentState.READY);
		IconSteps timerIcons = new IconSteps(test);
		timerIcons.startCheck(IconHelper.READY, 
				IconHelper.EXECUTING, IconHelper.SLEEPING, 
				IconHelper.STARTED, IconHelper.SLEEPING, 
				IconHelper.STOPPING, IconHelper.READY);
		
		test.run();
		
		checkFirstThreadFinished.checkWait();
		test.stop();
		
		timerStates.checkWait();
		timerIcons.checkNow();
		
		test.setJob(null);
		retry.destroy();
		test.destroy();
		
	}

	
	
	public void testStopBeforeTriggered() throws FailedToStopException {

		class Executor extends MockScheduledExecutorService {
			
			boolean canceled;
			Runnable job;
			
			@Override
			public ScheduledFuture<?> schedule(Runnable command, long delay,
					TimeUnit unit) {
				job = command;
				return new MockScheduledFuture<Void>() {
					@Override
					public boolean cancel(boolean mayInterruptIfRunning) {
						assertEquals(false, mayInterruptIfRunning);
						canceled = true;
						job = null;
						return true;
					}
				};
			}
		}
		Executor executor = new Executor();
		
		Timer test = new Timer();
		test.setClock(new ManualClock("2011-09-30 00:10"));
		test.setScheduleExecutorService(executor);
		
		test.setJob(new FlagState());
		
		TimeSchedule schedule = new TimeSchedule();
		test.setSchedule(schedule);
		
		StateSteps timerStates = new StateSteps(test);
		timerStates.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.STARTED);
		IconSteps timerIcons = new IconSteps(test);
		timerIcons.startCheck(IconHelper.READY, IconHelper.EXECUTING, 
				IconHelper.SLEEPING);
		
		test.run();
		
		timerStates.checkNow();
		timerIcons.checkNow();
		
		timerStates.startCheck(ParentState.STARTED, ParentState.READY);
		timerIcons.startCheck(IconHelper.SLEEPING, IconHelper.STOPPING,
				IconHelper.READY);
		
		test.stop();
		
		timerStates.checkNow();
		timerIcons.checkNow();
		
		assertEquals(true, executor.canceled);
		
		timerStates.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.STARTED);
		
		test.run();

		timerStates.checkNow();
		
		timerStates.startCheck(ParentState.STARTED, ParentState.COMPLETE);
		
		executor.job.run();
		
		timerStates.checkNow();

		test.destroy();
		
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
	
		Oddjob oddjob1 = new Oddjob();
		oddjob1.setFile(dirs.relative("test/conf/persisted-schedule.xml"));
		
		oddjob1.setExport("clock", new ArooaObject(
				new ManualClock("2011-03-09 06:30")));
		oddjob1.setExport("work-dir", new ArooaObject( 
				dirs.relative("work")));
				
		oddjob1.run();
				
		assertEquals(ParentState.STARTED, 
				oddjob1.lastStateEvent().getState());
				
		assertEquals(new SimpleInterval(
				DateHelper.parseDateTime("2011-03-10 05:30"),
				DateHelper.parseDateTime("2011-03-10 06:30")), 
				new OddjobLookup(oddjob1).lookup("persisted-schedule/schedule1.current"));
		assertEquals(DateHelper.parseDateTime("2011-03-10 05:30"), 
				new OddjobLookup(oddjob1).lookup("persisted-schedule/schedule1.nextDue"));
		
		oddjob1.stop();
		
		assertEquals(ParentState.READY, oddjob1.lastStateEvent().getState());
		
		oddjob1.destroy();
		
		//
		// Second run
		
		Oddjob oddjob2 = new Oddjob();
		oddjob2.setFile(dirs.relative("test/conf/persisted-schedule.xml"));
		
		oddjob2.setExport("clock", new ArooaObject(
				new ManualClock("2011-03-10 07:00")));
		oddjob2.setExport("work-dir", new ArooaObject( 
				dirs.relative("work")));
		
		oddjob2.load();
		
		Oddjob innerOddjob2 = new OddjobLookup(oddjob2).lookup("persisted-schedule",
				Oddjob.class);

		innerOddjob2.load();
		
		Stateful scheduledJob2 = new OddjobLookup(innerOddjob2).lookup("scheduled-job", 
				Stateful.class);
		
		StateSteps scheduledJobState2 = new StateSteps(scheduledJob2);
		
		scheduledJobState2.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);
				
		oddjob2.run();

		scheduledJobState2.checkWait();
		
		String text2 = new OddjobLookup(oddjob2).lookup(
				"persisted-schedule/scheduled-job.text", String.class);

		assertEquals("Job schedule at 2011-03-10 05:30:00.000 " +
				"but running at 2011-03-10 07:00:00.000", 
				text2);
		
		oddjob2.stop();
		
		assertEquals(ParentState.READY, oddjob2.lastStateEvent().getState());
		
		oddjob2.destroy();
		
		//
		// Third run
		    
		Oddjob oddjob3 = new Oddjob();
		oddjob3.setFile(dirs.relative("test/conf/persisted-schedule.xml"));
		
		oddjob3.setExport("clock", new ArooaObject(
				new ManualClock("2011-03-10 08:00")));
		oddjob3.setExport("work-dir", new ArooaObject( 
				dirs.relative("work")));
		
		oddjob3.run();
		
		assertEquals(new SimpleInterval(
				DateHelper.parseDateTime("2011-03-11 05:30"),
				DateHelper.parseDateTime("2011-03-11 06:30")), 
				new OddjobLookup(oddjob3).lookup("persisted-schedule/schedule1.current"));
		assertEquals(DateHelper.parseDateTime("2011-03-11 05:30"), 
				new OddjobLookup(oddjob3).lookup("persisted-schedule/schedule1.nextDue"));
		
		String text3 = new OddjobLookup(oddjob3).lookup(
				"persisted-schedule/scheduled-job.text", String.class);

		assertEquals("Job schedule at 2011-03-10 05:30:00.000 " +
				"but running at 2011-03-10 07:00:00.000", 
				text3);
		
		oddjob3.stop();
		
		assertEquals(ParentState.READY, oddjob3.lastStateEvent().getState());
		
		oddjob3.destroy();
	}
	
	public void testTimerExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException, ParseException {
		
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/scheduling/TimerExample.xml",
    			getClass().getClassLoader()));
    	
    	oddjob.load();
    	
    	assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
    	
    	OddjobLookup lookup = new OddjobLookup(oddjob);
    	
    	Timer timer = lookup.lookup("timer", Timer.class);
    	
    	ManualClock clock = new ManualClock("2011-04-08 09:59:59.750"); 
    	timer.setClock(clock);
    	
    	Stateful work = lookup.lookup("work", Stateful.class);

    	StateSteps workState = new StateSteps(work);
    	workState.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);
    	
    	oddjob.run();
    	
    	workState.checkWait();
    	
    	assertEquals(DateHelper.parseDateTime("2011-04-11 10:00"), 
    			timer.getNextDue());
    	
    	String result = lookup.lookup("work.text", String.class);
	
    	assertEquals("Doing some work at 2011-04-08 10:00:00.000",
    			result);
    	
    	oddjob.stop();
    	
    	assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
    	
    	//
    	// Run again.
    	
    	oddjob.run();
    	
    	assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());
    	assertEquals(JobState.COMPLETE, work.lastStateEvent().getState());
    	
    	assertEquals(DateHelper.parseDateTime("2011-04-11 10:00"), 
    			timer.getNextDue());
    	
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
    			ParentState.STARTED, 
    			ParentState.COMPLETE);
    	
    	oddjob.run();
    	
    	states.checkWait();
    	    	
    	oddjob.destroy();
	}
	
	private class OurOddjobExecutor extends MockOddjobExecutors {

		OurScheduledExecutorService executor = 
			new OurScheduledExecutorService();
		
		@Override
		public ScheduledExecutorService getScheduledExecutor() {
			return executor;
		}
		
		@Override
		public ExecutorService getPoolExecutor() {
			return null;
		}
		
	}
	
	public void testTimerStopJobExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {
		
		OurOddjobExecutor executors = new OurOddjobExecutor();
		
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/scheduling/TimerStopJobExample.xml",
    			getClass().getClassLoader()));
    	oddjob.setOddjobExecutors(executors);
    	
    	oddjob.load();
    	
    	Stateful wait = new OddjobLookup(oddjob).lookup(
    			"long-job", Stateful.class);
    	
    	StateSteps waitStates = new StateSteps(wait);
    	
    	waitStates.startCheck( 
    			JobState.READY, 
    			JobState.EXECUTING);
    	
    	new Thread(oddjob).start();
    	
    	waitStates.checkWait();
    	
    	assertTrue(executors.executor.delay > 9000);
    	
    	StateSteps states = new StateSteps(oddjob);
    	
    	states.startCheck( 
    			ParentState.EXECUTING, 
    			ParentState.STARTED, 
    			ParentState.COMPLETE);
    	
    	executors.executor.runnable.run();
    	
    	states.checkWait();
    	
    	oddjob.destroy();
    	
	}
	
	public void testTimerCrashPersistance() {
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <scheduling:timer id='timer' xmlns:scheduling='http://rgordon.co.uk/oddjob/scheduling'>" +
				"   <schedule>" +
				"    <schedules:daily at='07:00' xmlns:schedules='http://rgordon.co.uk/oddjob/schedules'/>" +
				"   </schedule>" +
				"   <clock>" +
				"    <value value='${clock}'/>" +
				"   </clock>" +
				"   <job>" +
				"    <echo>Hi</echo>" +
				"   </job>" +
				"  </scheduling:timer>" +
				" </job>" +
				"</oddjob>";
				 
		OurOddjobExecutor executors1 = new OurOddjobExecutor();
		
		MapPersister persister = new MapPersister();
		
		Oddjob oddjob1 = new Oddjob();
		oddjob1.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob1.setExport("clock", new ArooaObject(
				new ManualClock("2011-12-23 07:00")));
		oddjob1.setOddjobExecutors(executors1);
		oddjob1.setPersister(persister);
		oddjob1.run();
		
		assertEquals(ParentState.STARTED, oddjob1.lastStateEvent().getState());
		
		assertEquals(0, executors1.executor.delay);
		
		executors1.executor.runnable.run();
		
		assertEquals(24 * 60 * 60 * 1000L, executors1.executor.delay);
				
		OurOddjobExecutor executors2 = new OurOddjobExecutor();
		
		Oddjob oddjob2 = new Oddjob();
		oddjob2.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob2.setExport("clock", new ArooaObject(
				new ManualClock("2011-12-23 07:00")));
		oddjob2.setOddjobExecutors(executors2);
		oddjob2.setPersister(persister);
		oddjob2.run();
		
		assertEquals(24 * 60 * 60 * 1000L, executors2.executor.delay);		
	}
	
	private class OurFuture extends MockScheduledFuture<Void> {
		boolean cancelled;
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			this.cancelled = true;
			return true;
		}
	}
	
	public void testSetNextDue() throws ArooaPropertyException, ArooaConversionException, FailedToStopException, ParseException {
	
		ManualClock clock = new ManualClock("2012-12-27 08:00");
		
		OurFuture future = new OurFuture();
		OurOddjobExecutor executors = new OurOddjobExecutor();
		executors.executor.future = future;
		
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/scheduling/TimerSetNextDueExample.xml",
    			getClass().getClassLoader()));
    	oddjob.setOddjobExecutors(executors);
    	oddjob.setExport("clock", new ArooaObject(clock));

    	oddjob.run();
    	
    	assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());
    	
    	OddjobLookup lookup = new OddjobLookup(oddjob);
    	
    	Timer test = lookup.lookup("timer", Timer.class);
    	
    	assertEquals(DateHelper.parseDate("9999-12-31"), test.getNextDue());
    	
    	assertNotNull(executors.executor.runnable);
    	assertEquals(252045619200000L, executors.executor.delay);
    	assertEquals(false, future.cancelled);
    	
    	
    	Runnable set = lookup.lookup("set", Runnable.class);
    
    	set.run();
    	
    	assertEquals(DateHelper.parseDateTime("2012-12-27 08:02"), 
    			test.getNextDue());
    	
    	assertEquals((long) (2 * 60 * 1000), executors.executor.delay);
    	assertEquals(true, future.cancelled);
    	
    	executors.executor.runnable.run();
    	
    	assertEquals("Running at 9999-12-31 00:00:00.000", lookup.lookup("echo.text"));
    	
    	oddjob.stop();
    	
    	assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
    	
    	
    	oddjob.destroy();
	}
	
	public void testSetReshedule() throws ArooaPropertyException, ArooaConversionException, FailedToStopException, ParseException {
		
		ManualClock clock = new ManualClock("2013-01-16 08:00");
		
		OurFuture future = new OurFuture();
		OurOddjobExecutor executors = new OurOddjobExecutor();
		executors.executor.future = future;
		
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/scheduling/TimerSetRescheduleExample.xml",
    			getClass().getClassLoader()));
    	oddjob.setOddjobExecutors(executors);
    	oddjob.setExport("clock", new ArooaObject(clock));

    	oddjob.run();
    	
    	assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());
    	
    	OddjobLookup lookup = new OddjobLookup(oddjob);
    	
    	Timer test = lookup.lookup("timer", Timer.class);
    	
    	assertEquals(DateHelper.parseDateTime("2013-01-16 23:00:"), 
    			test.getNextDue());
    	
    	assertNotNull(executors.executor.runnable);
    	assertEquals(15 * 60 * 60 * 1000L, executors.executor.delay);
    	assertEquals(false, future.cancelled);
    	
    	Runnable set = lookup.lookup("set", Runnable.class);
    
    	set.run();
    	
    	assertEquals(DateHelper.parseDateTime("2013-01-17 23:00"), 
    			test.getNextDue());
    	
    	assertEquals((long) (39 * 60 * 60 * 1000L), executors.executor.delay);
    	assertEquals(true, future.cancelled);
    	
    	executors.executor.runnable.run();
    	
    	assertEquals("Running at 2013-01-17 23:00:00.000", 
    			lookup.lookup("echo.text"));
    	
    	oddjob.stop();
    	
    	assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
    	
    	
    	oddjob.destroy();
	}
}
