package org.oddjob.scheduling;
import org.junit.Before;

import org.junit.Test;

import java.beans.PropertyVetoException;
import java.text.ParseException;
import java.util.Date;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.extend.SerializableJob;
import org.oddjob.persist.ArchiveBrowserJob;
import org.oddjob.persist.MapPersister;
import org.oddjob.scheduling.state.TimerState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ManualClock;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.Clock;


/**
 * Timer and Retry will often be used in combination. Here's some
 * examples.
 * 
 * @author rob
 *
 */
public class TimerRetryCombinationTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(TimerRetryCombinationTest.class);
	
    @Before
    public void setUp() throws Exception {

		logger.info("---------------------  " + getName() + "  --------------");
	}
	
	public static class Results extends SerializableJob {
		private static final long serialVersionUID = 2009081400L;
		
		private volatile int soft;
		
		private volatile int hard;
		
		private volatile int executions;
		
		private volatile int result;
		
		@Override
		protected int execute() throws Throwable {
			logger.info("RUNNING!!!");
			++executions;
			return result;
		}
	
		@Override
		public boolean hardReset() {
			if (super.hardReset()) {
				logger.info("HARD RESET");
				synchronized (this) {
					hard++;
				}
				return true;
			}
			return false;
		}
		
		@Override
		public boolean softReset() {
			if (super.softReset()) {
				logger.info("SOFT RESET");
				synchronized (this) {
					soft++;
				}
				return true;
			}
			return false;
		}
		
		public synchronized int getSoft() {
			return soft;
		}
		
		public synchronized int getHard() {
			return hard;
		}
		
		public int getExecutions() {
			return executions;
		}

		public int getResult() {
			return result;
		}

		public void setResult(int result) {
			this.result = result;
		}
	}
	
	
   @Test
	public void testContextReset() throws ArooaConversionException, PropertyVetoException, InterruptedException {
	
		XMLConfiguration config = new XMLConfiguration(
				"org/oddjob/scheduling/TimerRetryCombinationTest1.xml",
				getClass().getClassLoader());
		
		DefaultExecutors services = new DefaultExecutors();
		MapPersister persister = new MapPersister();
		persister.setPath("testContextReset");
		
		Oddjob oddjob1 = new Oddjob();
		oddjob1.setOddjobExecutors(services);
		oddjob1.setConfiguration(config);
		oddjob1.setPersister(persister);
		
//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setOddjob(oddjob1);
//		explorer.setArooaSession(new StandardArooaSession());
//		explorer.run();
		
		logger.info("First Oddjob, starting first run.");
		
		StateSteps oddjob1State = new StateSteps(oddjob1);
		oddjob1State.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE, ParentState.INCOMPLETE);
		oddjob1.run();

		oddjob1State.checkWait();
		
		OddjobLookup lookup1 = new OddjobLookup(oddjob1);
		
		int hards1 = lookup1.lookup("results.hard", Integer.TYPE);
		int softs1 = lookup1.lookup("results.soft", Integer.TYPE);
		int executions = lookup1.lookup("results.executions", Integer.TYPE);
		
		assertEquals(4, hards1);
		assertEquals(8, softs1);
		assertEquals(8, executions);

		oddjob1.softReset();
		
		hards1 = lookup1.lookup("results.hard", Integer.TYPE);
		softs1 = lookup1.lookup("results.soft", Integer.TYPE);
		executions = lookup1.lookup("results.executions", Integer.TYPE);
		
		assertEquals(4, hards1);
		assertEquals(9, softs1);
		assertEquals(8, executions);
		
		logger.info("First Oddjob, starting second run.");
		
		oddjob1State.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE, ParentState.INCOMPLETE);
		
		oddjob1.run();
		
		oddjob1State.checkWait();
		
		hards1 = lookup1.lookup("results.hard", Integer.TYPE);
		softs1 = lookup1.lookup("results.soft", Integer.TYPE);
		executions = lookup1.lookup("results.executions", Integer.TYPE);

		assertEquals(8, hards1);
		assertEquals(17, softs1);
		assertEquals(16, executions);
		
		Resetable timer = lookup1.lookup("timer", Resetable.class);
		timer.hardReset();
		
		oddjob1.destroy();
		
		Oddjob oddjob2 = new Oddjob();
		oddjob2.setOddjobExecutors(services);
		oddjob2.setConfiguration(config);
		oddjob2.setPersister(persister);
		
		logger.info("Second Oddjob, starting first run.");
		
		StateSteps oddjob2State = new StateSteps(oddjob2);
		oddjob2State.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE, ParentState.INCOMPLETE);
		
		oddjob2.run();
		
		oddjob2State.checkWait();
		
		OddjobLookup lookup2 = new OddjobLookup(oddjob2);
		
		int hards2 = lookup2.lookup("results.hard", Integer.TYPE);
		int softs2 = lookup2.lookup("results.soft", Integer.TYPE);
		int executions2 = lookup2.lookup("results.executions", Integer.TYPE);
		
		assertEquals(12, hards2);
		assertEquals(25, softs2);
		assertEquals(24, executions2);
		
		oddjob2.destroy();
		
		services.stop();
	}
	
   @Test
	public void testStateNotifications() throws FailedToStopException, InterruptedException {
		
		XMLConfiguration config = new XMLConfiguration(
				"org/oddjob/scheduling/TimerRetryCombinationTest1.xml",
				getClass().getClassLoader());

		DefaultExecutors services = new DefaultExecutors();
		
		Oddjob oddjob1 = new Oddjob();
		oddjob1.setOddjobExecutors(services);
		oddjob1.setConfiguration(config);

		StateSteps ojStates = new StateSteps(oddjob1);
		
		oddjob1.load();
		
		Timer timer = (Timer) new OddjobLookup(oddjob1).lookup("timer");
		
		StateSteps timerStates = new StateSteps(timer);
		
		Retry retry = (Retry) new OddjobLookup(oddjob1).lookup("retry");
		
		StateSteps retryStates = new StateSteps(retry);
		
		ojStates.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE, ParentState.INCOMPLETE);
		
		timerStates.startCheck(TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.ACTIVE, TimerState.INCOMPLETE);
		
		retryStates.startCheck(TimerState.STARTABLE, TimerState.STARTING,
				TimerState.ACTIVE, TimerState.INCOMPLETE, 
				TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.ACTIVE, TimerState.INCOMPLETE, 
				TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.ACTIVE, TimerState.INCOMPLETE, 
				TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.ACTIVE, TimerState.INCOMPLETE);
				
		oddjob1.run();

		ojStates.checkWait();
		
		// Note we still need to wait because these state listeners maybe
		// notified after Oddjob has received the incomplete state.
		timerStates.checkWait();
		retryStates.checkWait();
		
		logger.info("Cleaning Up.");
		
		oddjob1.destroy();
		
		services.stop();
	}

	private class OurClock implements Clock {

		Date date;
		
		@Override
		public Date getDate() {
			return date;
		}
	}
	
	
	/**
	 * Tracking down a bug where examples didn't run when catching up.
	 * @throws ParseException 
	 * @throws InterruptedException 
	 */
   @Test
	public void testRetriesWithCatchup() throws ArooaConversionException, PropertyVetoException, ParseException, InterruptedException {
		
		XMLConfiguration config = new XMLConfiguration(
				"org/oddjob/scheduling/TimerRetryCombinationTest2.xml",
				getClass().getClassLoader());
		
		MapPersister persister = new MapPersister();
		persister.setPath("testContextReset");
		
		OurClock clock = new OurClock();
		clock.date = DateHelper.parseDateTime("2010-07-11 07:00");
		
		Oddjob oddjob1 = new Oddjob();
		oddjob1.setConfiguration(config);
		oddjob1.setPersister(persister);
		oddjob1.setExport("clock", new ArooaObject(clock));
		
		StateSteps oddjob1States = new StateSteps(oddjob1);
		oddjob1States.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.ACTIVE, ParentState.STARTED);
		
		logger.info("** Starting First Oddjob. **");
		
		oddjob1.run();

		oddjob1States.checkWait();
		
//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setOddjob(oddjob1);
//		explorer.setArooaSession(new StandardArooaSession());
//		explorer.run();
		
		final OddjobLookup lookup1 = new OddjobLookup(oddjob1);
		
		assertEquals(DateHelper.parseDateTime("2010-07-12 07:00"), 
				lookup1.lookup("timer.nextDue", Date.class)); 
				
		int hards1 = lookup1.lookup("results.hard", Integer.TYPE);
		int softs1 = lookup1.lookup("results.soft", Integer.TYPE);
		int executions1 = lookup1.lookup("results.executions", Integer.TYPE);
		
		assertEquals(1, hards1);
		assertEquals(1, softs1);
		assertEquals(1, executions1);
		
		// The archive state listener is added after oddjob child state
		// reflector so the archiver could still be archiving at this
		// point. This will ensure all listeners have fired before we
		// destroy.
		assertEquals(TimerState.COMPLETE, lookup1.lookup("retry", 
				Stateful.class).lastStateEvent().getState());
		
		oddjob1.destroy();
		
		Oddjob oddjob2 = new Oddjob();
		oddjob2.setConfiguration(config);
		oddjob2.setPersister(persister);
		oddjob2.setExport("clock", new ArooaObject(clock));
		
		clock.date = DateHelper.parseDateTime("2010-07-15 07:00");
		
		StateSteps oddjob2States = new StateSteps(oddjob2);
		oddjob2States.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.ACTIVE, ParentState.STARTED);
		
		logger.info("** Starting second Oddjob. **");
		
		oddjob2.run();
		
		oddjob2States.checkWait();
		
		final OddjobLookup lookup2 = new OddjobLookup(oddjob2);
		
		assertEquals(DateHelper.parseDateTime("2010-07-16 07:00"), 
				lookup2.lookup("timer.nextDue", Date.class)); 
				
		int hards2 = lookup2.lookup("results.hard", Integer.TYPE);
		int softs2 = lookup2.lookup("results.soft", Integer.TYPE);
		int executions2 = lookup2.lookup("results.executions", Integer.TYPE);
		
		assertEquals(5, hards2);
		assertEquals(5, softs2);
		assertEquals(5, executions2);
		
		// Ensure archiver finished persisting.
		assertEquals(TimerState.COMPLETE, lookup2.lookup("retry", 
				Stateful.class).lastStateEvent().getState());
		
		ArchiveBrowserJob browser = new ArchiveBrowserJob();
		browser.setArchiver(persister);
		browser.setArchiveName("the-archive");
		
		logger.info("** Running Archive Browser. **");
		
		browser.run();
		
		Object[] children = OddjobTestHelper.getChildren(browser);
		
		assertEquals(5, children.length);
		assertEquals("20100711", children[0].toString());
		assertEquals("20100712", children[1].toString());
		assertEquals("20100713", children[2].toString());
		assertEquals("20100714", children[3].toString());
		assertEquals("20100715", children[4].toString());
		
		oddjob2.destroy();
	}
	
   @Test
	public void testSimpleTimerRetryExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException, ParseException, FailedToStopException {
		
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/scheduling/SimpleTimerWithRetry.xml",
				getClass().getClassLoader()));
		
		oddjob.load();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Timer timer = lookup.lookup("timer", Timer.class);
		
		timer.setClock(new ManualClock("2011-09-30 12:00"));

		Stateful flagJob = lookup.lookup("flag-job", Stateful.class);
		
		StateSteps states = new StateSteps(flagJob);
		
		states.startCheck(JobState.READY, JobState.EXECUTING, JobState.EXCEPTION,
				JobState.READY, JobState.EXECUTING, JobState.EXCEPTION);
		
		oddjob.run();
		
		states.checkWait();
		
		assertEquals(DateHelper.parseDateTime("2011-10-01 08:00"),
				timer.getNextDue());		
		
		oddjob.stop();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		timer.setClock(new ManualClock("2011-10-01 07:59:59.990"));
		
		states.startCheck(JobState.EXCEPTION, JobState.READY, 
				JobState.EXECUTING, JobState.EXCEPTION,
				JobState.READY, JobState.EXECUTING, JobState.EXCEPTION);
		
		oddjob.run();
		
		states.checkWait();
		
		assertEquals(DateHelper.parseDateTime("2011-10-02 08:00"),
				timer.getNextDue());		
		
		oddjob.destroy();
	}
	
}
