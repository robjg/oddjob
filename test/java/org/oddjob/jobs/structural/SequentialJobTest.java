/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs.structural;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.framework.SimpleJob;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;
import org.oddjob.state.MirrorState;

/**
 * 
 */
public class SequentialJobTest extends TestCase {

	public static class OurJob extends SimpleJob {

		@Override
		protected int execute() throws Throwable {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
	
	// an empty sequence must be ready. This is to agree with oddjob
	// which must also be ready when reset and empty.
	// this is really a bug in StatefulChildHelper. An empty sequence should
	// be ready until run and then be complete. I think.
	public void testEmpty() {
		SequentialJob j = new SequentialJob();
		
		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());
		
		j.run();
		
		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());	
	}
	
	// a sequence of just objects will always be complete when it runs
	public void testObject() {
		SequentialJob j = new SequentialJob();
		
		j.setJobs(0, (new Object()));
		j.setJobs(1, (new Object()));
		
		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());
		j.run();
		assertEquals(JobState.COMPLETE, j.lastJobStateEvent().getJobState());	
		
	}
	
	public void testTriggers() {
		OurJob j1 = new OurJob();
		MirrorState t1 = new MirrorState();
		t1.setJob((Stateful) j1);
		t1.run();
		
		OurJob j2 = new OurJob();
		MirrorState t2 = new MirrorState();
		t2.setJob((Stateful) j2);
		t2.run();
		
		SequentialJob j = new SequentialJob();
		j.setJobs(0, t1);
		j.setJobs(1, t2);
		
		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());
		
		j.run();
		
		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());	
		
		((Runnable) j1).run();
		
		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());	
		
		((Runnable) j2).run();
		
		assertEquals(JobState.COMPLETE, j.lastJobStateEvent().getJobState());	
		
		((Resetable) j2).hardReset();

		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());	
	}
	
	public void testRunnable() {
		OurJob j1 = new OurJob();
		
		OurJob j2 = new OurJob();
		
		SequentialJob j = new SequentialJob();
		j.setJobs(0, j1);
		j.setJobs(1, j2);
		
		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());
		j.run();
		
		assertEquals(JobState.COMPLETE, j.lastJobStateEvent().getJobState());	
		
		((Resetable) j2).hardReset();

		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());			
	}
	
	/**
	 * Test a mixture of Objects and jobs.
	 */
	public void testMixture() {
		OurJob j1 = new OurJob();
		
		Object j2 = new OurJob();
		
		MirrorState t2 = new MirrorState();
		t2.setJob((Stateful) j2);
		t2.run();
		
		SequentialJob j = new SequentialJob();
		j.setJobs(0, j1);
		j.setJobs(1, t2);
		j.setJobs(2, new Object());

		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());
		j.run();
		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());	
		
		((Runnable) j2).run();
		
		assertEquals(JobState.COMPLETE, j.lastJobStateEvent().getJobState());	
		
		j.hardReset();

		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());			
	}
	
	public void testNotComplete() {
		FlagState j1 = new FlagState();
		j1.setState(JobState.COMPLETE);
		
		FlagState j2 = new FlagState();
		j2.setState(JobState.INCOMPLETE);
		
		SequentialJob j = new SequentialJob();
		j.setJobs(0, j1);
		j.setJobs(1, j2);

		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());
		j.run();
		
		assertEquals(JobState.INCOMPLETE, j.lastJobStateEvent().getJobState());	
		
		j.hardReset();

		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());			
		
	}
	
	public void testException() {
		FlagState j1 = new FlagState();
		j1.setState(JobState.COMPLETE);
		
		FlagState j2 = new FlagState();
		j2.setState(JobState.EXCEPTION);
		
		SequentialJob j = new SequentialJob();
		j.setJobs(0, j1);
		j.setJobs(1, j2);

		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());
		j.run();
		
		assertEquals(JobState.EXCEPTION, j.lastJobStateEvent().getJobState());	
		
		j.hardReset();

		assertEquals(JobState.READY, j.lastJobStateEvent().getJobState());			
		
	}
	
	public void testDestroyed() {
		
		FlagState j1 = new FlagState();
		j1.setState(JobState.COMPLETE);
		
		SequentialJob test = new SequentialJob();
		test.setJobs(0, j1);

		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		test.run();
		
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
	}
}
