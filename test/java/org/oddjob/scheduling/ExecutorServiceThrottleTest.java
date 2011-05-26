package org.oddjob.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.jobs.WaitJob;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

public class ExecutorServiceThrottleTest extends TestCase {
	private static final Logger logger = Logger.getLogger(ExecutorServiceThrottle.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("---------------  " + getName() + "  -----------------");
	}
	
	private class ExecutingCounter implements JobStateListener {
		
		private final List<Stateful[]> exceptions = new ArrayList<Stateful[]>();
		
		private final List<Stateful> executing = new ArrayList<Stateful>();
		
		@Override
		public void jobStateChange(JobStateEvent event) {
			synchronized(executing) {
				switch (event.getJobState()) {
				case COMPLETE:
					executing.remove(event.getSource());
					break;
				case READY:
					break;
				case EXECUTING:
					executing.add(event.getSource());
					if (executing.size() > 3) {
						exceptions.add(executing.toArray(
								new Stateful[executing.size()]));
					}
					break;
				default:
					exceptions.add(new Stateful[] { event.getSource() });					
				}
			}
			
		}
	}
	
	
	public void testQuickJobs() throws InterruptedException, ExecutionException {
		
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		
		ExecutingCounter counter = new ExecutingCounter();
		
		ExecutorServiceThrottle throttle = new ExecutorServiceThrottle(
				executorService, 3);

		FlagState[] jobs = new FlagState[100];
		Future<?>[] futures = new Future<?>[100];
		
		for (int i = 0; i < 100; ++i) {
		
			jobs[i] = new FlagState();
			jobs[i].setName("Job " + i);
			
			jobs[i].addJobStateListener(counter);
			
			futures[i] = throttle.submit(jobs[i]);
		}
		
		for (int i = 0; i < 100; ++i) {
			futures[i].get();
			assertEquals(JobState.COMPLETE, 
					jobs[i].lastJobStateEvent().getJobState());
		}
	
		assertEquals(0, counter.exceptions.size());
		
		executorService.shutdown();
	}
	
	public void testSlowJobs() throws InterruptedException, ExecutionException, FailedToStopException {
		
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		
		
		ExecutorServiceThrottle throttle = new ExecutorServiceThrottle(
				executorService, 2);

		WaitJob w1 = new WaitJob();
		WaitJob w2 = new WaitJob();
		WaitJob w3 = new WaitJob();
		WaitJob w4 = new WaitJob();
		WaitJob w5 = new WaitJob();

		w1.setName("Wait 1");
		w2.setName("Wait 2");
		w3.setName("Wait 3");
		w4.setName("Wait 4");
		w5.setName("Wait 5");
		
		StateSteps s1 = new StateSteps(w1);
		StateSteps s2 = new StateSteps(w2);
		StateSteps s3 = new StateSteps(w3);
		StateSteps s4 = new StateSteps(w4);
		StateSteps s5 = new StateSteps(w5);
		
		s1.startCheck(JobState.READY, JobState.EXECUTING);
		s2.startCheck(JobState.READY, JobState.EXECUTING);
		s3.startCheck(JobState.READY, JobState.EXECUTING);
		s4.startCheck(JobState.READY, JobState.EXECUTING);
		s5.startCheck(JobState.READY, JobState.EXECUTING);
		
		throttle.submit(w1);
		throttle.submit(w2);
		throttle.submit(w3);
		throttle.submit(w4);
		throttle.submit(w5);

		logger.info("Waiting for w1 and w2 to start.");
		
		s1.checkWait();
		s2.checkWait();

		assertEquals(JobState.READY, w3.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, w4.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, w5.lastJobStateEvent().getJobState());
		
		w1.stop();
		
		logger.info("Waiting for w3 to start.");
		
		s3.checkWait();
		
		assertEquals(JobState.READY, w4.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, w5.lastJobStateEvent().getJobState());
		
		w3.stop();
		
		logger.info("Waiting for w4 to start.");
		
		s4.checkWait();
		
		assertEquals(JobState.READY, w5.lastJobStateEvent().getJobState());

		w4.stop();
		
		logger.info("Waiting for w5 to start.");
		
		s5.checkWait();
		
		w2.stop();
		w5.stop();
		
		executorService.shutdown();
	}
	
	
	public void testPendingJobsWhenShutdown() throws InterruptedException, ExecutionException, FailedToStopException {
		
		ExecutorService executorService = Executors.newFixedThreadPool(5);		
		
		ExecutorServiceThrottle throttle = new ExecutorServiceThrottle(
				executorService, 1);

		WaitJob wait1 = new WaitJob();
		WaitJob wait2 = new WaitJob();

		wait1.setName("Wait 1");
		wait2.setName("Wait 2");
		
		StateSteps stateCheck1 = new StateSteps(wait1);
		StateSteps stateCheck2 = new StateSteps(wait2);
		
		stateCheck1.startCheck(JobState.READY, JobState.EXECUTING);
		stateCheck2.startCheck(JobState.READY, JobState.EXECUTING);
		
		Future<?> future1 = throttle.submit(wait1);
		
		throttle.submit(wait2);

		logger.info("Waiting for w1 to start.");
		
		stateCheck1.checkWait();
				
		executorService.shutdown();
		
		stateCheck1.startCheck(JobState.EXECUTING, JobState.COMPLETE);
		
		wait1.stop();
		
		stateCheck1.checkWait();
		
		future1.get();
		
		executorService.awaitTermination(1, TimeUnit.HOURS);
		
		assertTrue(executorService.isTerminated());
		
		assertEquals(JobState.READY, wait2.lastJobStateEvent().getJobState());
	}
	
	public void testCancelledWork() throws InterruptedException, ExecutionException, FailedToStopException {
		
		ExecutorService executorService = Executors.newFixedThreadPool(5);		
		
		ExecutorServiceThrottle throttle = new ExecutorServiceThrottle(
				executorService, 1);

		WaitJob w1 = new WaitJob();
		WaitJob w2 = new WaitJob();

		w1.setName("Wait 1");
		w2.setName("Wait 2");
		
		StateSteps s1 = new StateSteps(w1);
		StateSteps s2 = new StateSteps(w2);
		
		s1.startCheck(JobState.READY, JobState.EXECUTING);
		s2.startCheck(JobState.READY, JobState.EXECUTING);
		
		Future<?> f1 = throttle.submit(w1);
		
		Future<?> f2 = throttle.submit(w2);

		logger.info("Waiting for w1 to start.");
		
		s1.checkWait();
				
		f2.cancel(false);
		
		s1.startCheck(JobState.EXECUTING, JobState.COMPLETE);
		
		w1.stop();
		
		s1.checkWait();
		
		f1.get();
		
		assertEquals(JobState.READY, w2.lastJobStateEvent().getJobState());
		
		executorService.shutdown();		
	}
}
