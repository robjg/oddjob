/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.Resetable;
import org.oddjob.StateSteps;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.framework.StopWait;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

/**
 * 
 */
public class CascadeJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(CascadeJobTest.class);
	
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
		DefaultExecutors executors = new DefaultExecutors();

		CascadeJob test = new CascadeJob();
		test.setExecutorService(executors.getPoolExecutor());
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		test.run();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());	
		
		executors.stop();
	}
		
	public void testSimpleRunnables() throws FailedToStopException, InterruptedException {
		DefaultExecutors executors = new DefaultExecutors();

		OurJob job1 = new OurJob();
		OurJob job2 = new OurJob();
		OurJob job3 = new OurJob();
		
		CascadeJob test = new CascadeJob();
		test.setExecutorService(executors.getPoolExecutor());
		
		test.setJobs(0, job1);
		test.setJobs(1, job2);
		test.setJobs(2, job3);
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		StateSteps testState = new StateSteps(test);
		
		testState.startCheck(JobState.READY, 
				JobState.EXECUTING, JobState.COMPLETE);
		
		test.run();
		
		testState.checkWait();
		
		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());	
		assertEquals(JobState.COMPLETE, job2.lastJobStateEvent().getJobState());	
		assertEquals(JobState.COMPLETE, job3.lastJobStateEvent().getJobState());			
		
		assertEquals(1, job1.ran);
		assertEquals(1, job2.ran);
		assertEquals(1, job3.ran);
		
		((Resetable) job2).hardReset();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());	
		assertEquals(JobState.READY, job2.lastJobStateEvent().getJobState());	
		assertEquals(JobState.COMPLETE, job3.lastJobStateEvent().getJobState());	
		
		test.hardReset();
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.READY, job1.lastJobStateEvent().getJobState());	
		assertEquals(JobState.READY, job2.lastJobStateEvent().getJobState());	
		assertEquals(JobState.READY, job3.lastJobStateEvent().getJobState());	
		
		testState.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);
		
		test.run();
		
		testState.checkWait();
		
		assertEquals(2, job1.ran);
		assertEquals(2, job2.ran);
		assertEquals(2, job3.ran);
		
		executors.stop();
	}
	
	
	public void testNotComplete() throws FailedToStopException {
		DefaultExecutors executors = new DefaultExecutors();
		
		FlagState job1 = new FlagState();
		job1.setState(JobState.INCOMPLETE);
		
		FlagState job2 = new FlagState();
		job2.setState(JobState.COMPLETE);
		
		CascadeJob test = new CascadeJob();
		test.setExecutorService(executors.getPoolExecutor());
		
		test.setJobs(0, job1);
		test.setJobs(1, job2);

		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		test.run();
		
		new StopWait(job1).run();
		
		assertEquals(JobState.EXECUTING, test.lastJobStateEvent().getJobState());	

		job1.setState(JobState.COMPLETE);
		job1.hardReset();

		job1.run();
		
		new StopWait(test).run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());			
		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());			
		assertEquals(JobState.COMPLETE, job2.lastJobStateEvent().getJobState());			
		
		executors.stop();
	}
	
	public void testException() throws FailedToStopException, InterruptedException {
		DefaultExecutors executors = new DefaultExecutors();
		
		FlagState job1 = new FlagState();
		job1.setState(JobState.COMPLETE);
		
		FlagState job2 = new FlagState();
		job2.setState(JobState.EXCEPTION);
				
		CascadeJob test = new CascadeJob();
		test.setExecutorService(executors.getPoolExecutor());
		
		test.setJobs(0, job1);
		test.setJobs(1, job2);

		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		StateSteps job2Check = new StateSteps(job2);
		
		job2Check.startCheck(JobState.READY, 
				JobState.EXECUTING, JobState.EXCEPTION);
		
		test.run();

		job2Check.checkWait();

		new StopWait(test).run();
		
		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());	
		assertEquals(JobState.EXCEPTION, job2.lastJobStateEvent().getJobState());	
		assertEquals(JobState.EXCEPTION, test.lastJobStateEvent().getJobState());	
		
		job2Check.startCheck(JobState.EXCEPTION, JobState.READY, 
				JobState.EXECUTING, JobState.COMPLETE);

		job2.setState(JobState.COMPLETE);
		job2.softReset();
		job2.run();
		
		job2Check.checkWait();
		
		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, job2.lastJobStateEvent().getJobState());
		
		executors.stop();
	}
	
	public void testDestroyed() throws FailedToStopException {
		DefaultExecutors executors = new DefaultExecutors();
		
		FlagState job1 = new FlagState();
		job1.setState(JobState.COMPLETE);
		
		CascadeJob test = new CascadeJob();
		test.setExecutorService(executors.getPoolExecutor());
		
		test.setJobs(0, job1);

		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		test.run();
		
		new StopWait(test).run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());	

		final List<JobState> results = new ArrayList<JobState>();
		
		class OurListener implements JobStateListener {
			
			public void jobStateChange(JobStateEvent event) {
				results.add(event.getJobState());
			}
		}
		OurListener l = new OurListener();
		test.addJobStateListener(l);
		
		assertEquals(JobState.COMPLETE, results.get(0));	
		assertEquals(1, results.size());
		
		test.destroy();

		assertEquals(JobState.DESTROYED, results.get(1));	
		assertEquals(2, results.size());
		
		executors.stop();
	}
	
	public void testRemovingAndInserting() throws InterruptedException {
		
		DefaultExecutors executors = new DefaultExecutors();
		
		FlagState job1 = new FlagState(JobState.COMPLETE);
		FlagState job2 = new FlagState(JobState.COMPLETE);
				
		CascadeJob test = new CascadeJob();
		test.setExecutorService(executors.getPoolExecutor());

		StateSteps testState = new StateSteps(test);		
		
		testState.startCheck(JobState.READY, JobState.EXECUTING, 
				JobState.COMPLETE);
		
		test.setJobs(0, job1);
		test.setJobs(1, job2);

		test.run();
		
		testState.checkWait();
		
		test.setJobs(0, null);
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		testState.startCheck(JobState.COMPLETE, JobState.EXECUTING, 
				JobState.COMPLETE);
		
		FlagState job3 = new FlagState(JobState.COMPLETE);
		
		StateSteps job3State = new StateSteps(job3);		
		
		job3State.startCheck(JobState.READY);
		
		test.setJobs(1, job3);
				
		job3State.checkNow();
		
		FlagState job4 = new FlagState(JobState.COMPLETE);
		
		test.setJobs(0, job4);
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, job4.lastJobStateEvent().getJobState());
		
	}
	
	public void testInOddjob() throws InterruptedException {
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:cascade>" +
			"   <jobs>" +
			"    <echo text='one'/>" +
			"    <echo text='two'/>" +
			"    <echo text='three'/>" +
			"   </jobs>" +
			"  </state:cascade>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		StateSteps oddjobStates = new StateSteps(oddjob);		
		
		oddjobStates.startCheck(JobState.READY, JobState.EXECUTING, 
				JobState.COMPLETE);
		
		oddjob.run();
		
		oddjobStates.checkWait();
		
		assertEquals(JobState.COMPLETE, oddjob.lastJobStateEvent().getJobState());
	}
}
