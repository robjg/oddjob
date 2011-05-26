/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.Resetable;
import org.oddjob.StateSteps;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.schedules.schedules.CountSchedule;
import org.oddjob.schedules.schedules.IntervalSchedule;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.Timer;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;

/**
 * 
 */
public class JoinJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(JoinJobTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("----------------  " + getName() + "  -----------------");
	}
	
	private static class OurJob extends SimpleJob {

		int ran;
		
		@Override
		protected int execute() throws Throwable {
			++ran;
			return 0;
		}
	}
	
	
	// an empty sequence must be ready. This is to agree with oddjob
	// which must also be ready when reset and empty.
	// this is really a bug in StatefulChildHelper. An empty sequence should
	// be ready until run and then be complete. I think.
	public void testEmpty() {

		JoinJob test = new JoinJob();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		test.run();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());	
		
	}
		
	public void testSimpleRunnable() throws FailedToStopException, InterruptedException {

		OurJob job1 = new OurJob();
		
		JoinJob test = new JoinJob();
		
		test.setJob(job1);
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		StateSteps testState = new StateSteps(test);
		
		testState.startCheck(JobState.READY, 
				JobState.EXECUTING, JobState.COMPLETE);
		
		test.run();
		
		testState.checkNow();
		
		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());
		
		assertEquals(1, job1.ran);
		
		((Resetable) job1).hardReset();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, job1.lastJobStateEvent().getJobState());	
				
		testState.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);
		
		test.run();
		
		testState.checkNow();
		
		assertEquals(2, job1.ran);
	}
	
	
	public void testNotComplete() throws FailedToStopException {
		
		FlagState job1 = new FlagState();
		job1.setState(JobState.INCOMPLETE);
		
		
		JoinJob test = new JoinJob();
		
		test.setJob(job1);

		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		test.run();
		
		assertEquals(JobState.INCOMPLETE, test.lastJobStateEvent().getJobState());	

		job1.setState(JobState.COMPLETE);
		job1.hardReset();

		job1.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());			
		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());			
	}
	
	public void testAsynchronous() throws FailedToStopException, InterruptedException {
		DefaultExecutors executors = new DefaultExecutors();
		
		FlagState job1 = new FlagState();
		job1.setState(JobState.COMPLETE);
		
		Timer timer = new Timer();
		
		CountSchedule count = new CountSchedule(1);
		IntervalSchedule interval = new IntervalSchedule(500);
		count.setRefinement(interval);
		timer.setSchedule(count);
		timer.setJob(job1);
		timer.setScheduleExecutorService(executors.getScheduledExecutor());
		
		JoinJob test = new JoinJob();
		
		test.setJob(timer);

		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		StateSteps testStates = new StateSteps(test);
		
		testStates.startCheck(JobState.READY, 
				JobState.EXECUTING, JobState.COMPLETE);
		
		test.run();

		testStates.checkNow();

		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());	
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());	
		
		executors.stop();
	}
	
	public void testAsynchronousStop() throws FailedToStopException, InterruptedException {
		DefaultExecutors executors = new DefaultExecutors();
		
		FlagState job1 = new FlagState();
		job1.setState(JobState.COMPLETE);
		
		Timer timer = new Timer();
		
		CountSchedule count = new CountSchedule(1);
		IntervalSchedule interval = new IntervalSchedule(1000000L);
		count.setRefinement(interval);
		timer.setSchedule(count);
		timer.setJob(job1);
		timer.setScheduleExecutorService(executors.getScheduledExecutor());
		
		final JoinJob test = new JoinJob();
		test.setJob(timer);
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		StateSteps testStates = new StateSteps(test);
		
		testStates.startCheck(JobState.READY, 
				JobState.EXECUTING, JobState.COMPLETE);
		
		
		executors.getScheduledExecutor().schedule(new Runnable() {
			@Override
			public void run() {
				try {
					test.stop();
				} catch (FailedToStopException e) {
					throw new RuntimeException(e);
				}
			}
		}, 500, TimeUnit.MILLISECONDS);
		
		
		test.run();

		testStates.checkNow();

		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());	
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());	
		
		executors.stop();
	}
	
	public void testDestroyed() throws FailedToStopException {
		
		FlagState job1 = new FlagState();
		job1.setState(JobState.COMPLETE);
		
		JoinJob test = new JoinJob();
		
		test.setJob(job1);

		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());	

		StateSteps testStates = new StateSteps(test);
		
		testStates.startCheck(JobState.COMPLETE, 
				JobState.DESTROYED);
		
		test.destroy();

		testStates.checkNow();
	}
	
	public void testInOddjob() {
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:join>" +
			"   <job>" +
			"    <echo text='hello'/>" +
			"   </job>" +
			"  </state:join>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		assertEquals(JobState.COMPLETE, oddjob.lastJobStateEvent().getJobState());
	}
}
