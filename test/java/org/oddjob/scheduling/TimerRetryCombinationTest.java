package org.oddjob.scheduling;

import java.beans.PropertyVetoException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SerializableJob;
import org.oddjob.framework.StopWait;
import org.oddjob.jobs.WaitJob;
import org.oddjob.persist.ArchiveBrowserJob;
import org.oddjob.persist.MapPersister;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;
import org.oddjob.util.Clock;


/**
 * Timer and Retry will often be used in combination. Here's some
 * examples.
 * 
 * @author rob
 *
 */
public class TimerRetryCombinationTest extends TestCase {
	private static final Logger logger = Logger.getLogger(TimerRetryCombinationTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.debug("--------------------- " + getName() + " --------------");
	}
	
	public static class Results extends SerializableJob {
		private static final long serialVersionUID = 2009081400L;
		
		private int soft;
		
		private int hard;
		
		private int executions;
		
		private int result;
		
		@Override
		protected int execute() throws Throwable {
			logger.info("RUNNING!!!");
			++executions;
			return result;
		}
	
		@Override
		public boolean hardReset() {
			if (super.hardReset()) {
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
	
	
	public void testContextReset() throws ArooaConversionException, PropertyVetoException {
	
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
		
		oddjob1.run();

		WaitJob wait1 = new WaitJob();
		wait1.setFor(oddjob1);
		wait1.setState("INCOMPLETE");
		
		wait1.run();
		
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
		
		oddjob1.run();
		
		wait1.hardReset();
		wait1.run();
		
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
		
		oddjob2.run();
		
		WaitJob wait2 = new WaitJob();
		wait2.setFor(oddjob2);
		wait2.setState("INCOMPLETE");
		wait2.run();
		
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
	
	private class RecordingStateListener implements JobStateListener {
				
		final List<JobStateEvent> eventList = new ArrayList<JobStateEvent>();
		
		public synchronized void jobStateChange(JobStateEvent event) {
			logger.info("Recording Event [" + event.getJobState() + "] for [" + 
					event.getSource() + "] index [" + eventList.size() + "]");
			eventList.add(event);
		}
		
		public synchronized JobStateEvent get(int index) {
			return eventList.get(index);
		}
		
		public synchronized int size() {
			return eventList.size();
		}
	}
	
	public void testStateNotifications() throws FailedToStopException {
		
		XMLConfiguration config = new XMLConfiguration(
				"org/oddjob/scheduling/TimerRetryCombinationTest1.xml",
				getClass().getClassLoader());

		DefaultExecutors services = new DefaultExecutors();
		
		Oddjob oddjob1 = new Oddjob();
		oddjob1.setOddjobExecutors(services);
		oddjob1.setConfiguration(config);

		RecordingStateListener ojRec = new RecordingStateListener();
		oddjob1.addJobStateListener(ojRec);
		
		assertEquals(1, ojRec.size());
		assertEquals(JobState.READY, ojRec.get(0).getJobState());
		
		oddjob1.load();
		
		assertEquals(1, ojRec.size());
		
		Timer timer = (Timer) new OddjobLookup(oddjob1).lookup("timer");
		
		RecordingStateListener timerRec = new RecordingStateListener();
		timer.addJobStateListener(timerRec);
				
		assertEquals(1, timerRec.size());
		assertEquals(JobState.READY, timerRec.get(0).getJobState());
		
		Retry retry = (Retry) new OddjobLookup(oddjob1).lookup("retry");
		
		RecordingStateListener retryRec = new RecordingStateListener();
		retry.addJobStateListener(retryRec);
		
		assertEquals(1, retryRec.size());
		assertEquals(JobState.READY, retryRec.get(0).getJobState());
		
		oddjob1.run();

		new StopWait(oddjob1).run();
		logger.info("Oddjob1 has stopped. State is: " + oddjob1.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.EXECUTING, ojRec.get(1).getJobState());
		assertEquals(JobState.INCOMPLETE, ojRec.get(2).getJobState());
		assertEquals(3, ojRec.size());
		
		// Not our recording listener is added after Oddjob's, so it's possible
		// that we haven't have all events yet. This wait ensures we have.
		new StopWait(timer).run();
		
		logger.info("Timer has stopped. State is: " + timer.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.EXECUTING, timerRec.get(1).getJobState());
		assertEquals(JobState.INCOMPLETE, timerRec.get(2).getJobState());
		assertEquals(3, timerRec.size());
		
		assertEquals(JobState.EXECUTING, retryRec.get(1).getJobState());
		assertEquals(JobState.INCOMPLETE, retryRec.get(2).getJobState());
		assertEquals(JobState.READY, retryRec.get(3).getJobState());
		assertEquals(JobState.EXECUTING, retryRec.get(4).getJobState());
		assertEquals(JobState.INCOMPLETE, retryRec.get(5).getJobState());
		assertEquals(JobState.READY, retryRec.get(6).getJobState());
		assertEquals(JobState.EXECUTING, retryRec.get(7).getJobState());
		assertEquals(JobState.INCOMPLETE, retryRec.get(8).getJobState());
		assertEquals(JobState.READY, retryRec.get(9).getJobState());
		assertEquals(JobState.EXECUTING, retryRec.get(10).getJobState());
		assertEquals(JobState.INCOMPLETE, retryRec.get(11).getJobState());
		assertEquals(12, retryRec.size());
		
		logger.info("Cleaning Up.");
		
		oddjob1.destroy();
		
		assertEquals(JobState.DESTROYED, retryRec.get(12).getJobState());
		assertEquals(13, retryRec.size());
		
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
		
		oddjob1.run();

		OddjobLookup lookup1 = new OddjobLookup(oddjob1);
		
		while (!DateHelper.parseDateTime("2010-07-12 07:00").equals(
				lookup1.lookup("timer.nextDue", Date.class))) {
			Thread.sleep(100);
		}
		
		int hards1 = lookup1.lookup("results.hard", Integer.TYPE);
		int softs1 = lookup1.lookup("results.soft", Integer.TYPE);
		int executions1 = lookup1.lookup("results.executions", Integer.TYPE);
		
		assertEquals(1, hards1);
		assertEquals(1, softs1);
		assertEquals(1, executions1);
		
//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setOddjob(oddjob1);
//		explorer.setArooaSession(new StandardArooaSession());
//		explorer.run();
		
		oddjob1.destroy();
		
		Oddjob oddjob2 = new Oddjob();
		oddjob2.setConfiguration(config);
		oddjob2.setPersister(persister);
		oddjob2.setExport("clock", new ArooaObject(clock));
		
		clock.date = DateHelper.parseDateTime("2010-07-15 07:00");
		
		oddjob2.hardReset();
		oddjob2.run();
		
		OddjobLookup lookup2 = new OddjobLookup(oddjob2);
		
		while (!DateHelper.parseDateTime("2010-07-16 07:00").equals(
				lookup2.lookup("timer.nextDue", Date.class))) {
			Thread.sleep(100);
		}
		
		int hards2 = lookup2.lookup("results.hard", Integer.TYPE);
		int softs2 = lookup2.lookup("results.soft", Integer.TYPE);
		int executions2 = lookup2.lookup("results.executions", Integer.TYPE);
		
		assertEquals(6, hards2);
		assertEquals(5, softs2);
		assertEquals(5, executions2);
		
		ArchiveBrowserJob browser = new ArchiveBrowserJob();
		browser.setArchiver(persister);
		browser.setArchiveName("the-archive");
		
		browser.run();
		
		Object[] children = Helper.getChildren(browser);
		
		assertEquals(5, children.length);
		assertEquals("20100711", children[0].toString());
		assertEquals("20100712", children[1].toString());
		assertEquals("20100713", children[2].toString());
		assertEquals("20100714", children[3].toString());
		assertEquals("20100715", children[4].toString());
		
		oddjob2.destroy();
	}
}
