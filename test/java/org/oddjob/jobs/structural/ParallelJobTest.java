package org.oddjob.jobs.structural;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.MockStateful;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.MockScheduledExecutorService;
import org.oddjob.scheduling.MockScheduledFuture;
import org.oddjob.state.FlagState;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.JobStateListener;

public class ParallelJobTest extends TestCase {

	public void testThreeJobs() {
	
		FlagState job1 = new FlagState(JobState.COMPLETE);
		FlagState job2 = new FlagState(JobState.COMPLETE);
		FlagState job3 = new FlagState(JobState.COMPLETE);

		DefaultExecutors defaultServices = new DefaultExecutors();
		
		ParallelJob test = new ParallelJob();
		
		test.setExecutorService(defaultServices.getPoolExecutor());

		test.setJobs(0, job1);
		test.setJobs(1, job2);
		test.setJobs(2, job3);
		
		test.run();
	
		assertEquals(JobState.COMPLETE, job3.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, job2.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		test.hardReset();
		
		assertEquals(JobState.READY, job1.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, job2.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, job3.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		test.run();
		
		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, job2.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, job3.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
	
		defaultServices.stop();
	}
	
	public void testThrottledExecution() {
		
		FlagState job1 = new FlagState(JobState.COMPLETE);
		FlagState job2 = new FlagState(JobState.COMPLETE);
		FlagState job3 = new FlagState(JobState.COMPLETE);

		DefaultExecutors defaultServices = new DefaultExecutors();
		defaultServices.setPoolSize(1);
		
		ParallelJob test = new ParallelJob();
		
		test.setExecutorService(defaultServices.getPoolExecutor());

		test.setJobs(0, job1);
		test.setJobs(0, job2);
		test.setJobs(0, job3);
		
		test.run();
	
		assertEquals(JobState.COMPLETE, job3.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, job2.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, job1.lastJobStateEvent().getJobState());
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
	}
	
	private class OurJob extends SimpleJob 
	implements Stoppable {
		
		@Override
		protected int execute() throws Throwable {
			new Thread() {
				public void run() {
					try {
						OurJob.this.stop();
					} catch (FailedToStopException e) {
						e.printStackTrace();
					}
				};
			}.start();
			while(!stop) {
				try {
					synchronized (this) {
						wait(1000);					
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			return 0;
		}
	}
	
	private class OurPoolExecutor extends MockScheduledExecutorService {

		public Future<?> submit(Runnable runnable) {
					runnable.run();
			return new MockScheduledFuture<Void>() {
				@Override
				public Void get() {
					return null;
				}
			};
		}
	}
	
	public void testStop() {
		
		ParallelJob test = new ParallelJob();
		test.setExecutorService(new OurPoolExecutor());

		OurJob job = new OurJob();
		
		test.setJobs(0, job);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		test.destroy();
	}
	
	private class DestroyJob extends MockStateful 
	implements Runnable {
		
		JobStateHandler handler = new JobStateHandler(this);
		
		public void addJobStateListener(JobStateListener listener) {
			handler.addJobStateListener(listener);
		}
		public void removeJobStateListener(JobStateListener listener) {
			handler.removeJobStateListener(listener);
		}
		
		public void run() {
			handler.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
					handler.setJobState(JobState.COMPLETE);
					handler.fireEvent();
				}
			});
		}
		
		void destroy() {
			handler.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
					handler.setJobState(JobState.DESTROYED);
					handler.fireEvent();
				}
			});
		}
	}
	
	/**
	 * Child state changes before null is set as the child. Parrallel job
	 * shouln't get that state.
	 */
	public void testChildDestroyed() {
		
		ParallelJob test = new ParallelJob();
		test.setExecutorService(new OurPoolExecutor());

		DestroyJob destroy = new DestroyJob();
		
		test.setJobs(0, destroy);
		
		test.run();
		
		JobStateEvent event = test.lastJobStateEvent();
		assertEquals(JobState.COMPLETE, event.getJobState());
		
		destroy.destroy();
		
		assertEquals(event, test.lastJobStateEvent());
		
	}
	
	private class RecordingStateListener implements JobStateListener {
		
		List<JobStateEvent> events = new ArrayList<JobStateEvent>();
		
		public void jobStateChange(JobStateEvent event) {
			events.add(event);
		}
	}
	
	public void testStateNotifications() {
		
		FlagState job1 = new FlagState(JobState.COMPLETE);
		FlagState job2 = new FlagState(JobState.COMPLETE);
		FlagState job3 = new FlagState(JobState.COMPLETE);

		DefaultExecutors defaultServices = new DefaultExecutors();
		
		ParallelJob test = new ParallelJob();
		
		RecordingStateListener recorder = new RecordingStateListener();
		test.addJobStateListener(recorder);
		
		assertEquals(1, recorder.events.size());
		assertEquals(JobState.READY, recorder.events.get(0).getJobState());
		
		test.setExecutorService(defaultServices.getPoolExecutor());

		test.setJobs(0, job1);
		test.setJobs(1, job2);
		test.setJobs(2, job3);
		
		test.run();

		assertEquals(3, recorder.events.size());
		assertEquals(JobState.EXECUTING, recorder.events.get(1).getJobState());
		assertEquals(JobState.COMPLETE, recorder.events.get(2).getJobState());
		
		test.hardReset();
		
		assertEquals(4, recorder.events.size());
		assertEquals(JobState.READY, recorder.events.get(3).getJobState());
		
		test.run();
		
		assertEquals(6, recorder.events.size());
		assertEquals(JobState.EXECUTING, recorder.events.get(4).getJobState());
		assertEquals(JobState.COMPLETE, recorder.events.get(5).getJobState());
	
		defaultServices.stop();
	}
	
	public void testInOddjob() {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <parallel>" +
            "   <jobs>" + 
            "    <echo text='a'/>" +
            "    <echo text='b'/>" +
            "   </jobs>" +
			"  </parallel>" +
			" </job>" + 
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		assertEquals(JobState.COMPLETE, 
				oddjob.lastJobStateEvent().getJobState());
		
		oddjob.destroy();
	}
	
	public void stop() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <parallel>" +
            "   <jobs>" + 
            "    <wait id='wait1'/>" +
            "    <wait id='wait2'/>" +
            "   </jobs>" +
			"  </parallel>" +
			" </job>" + 
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.load();
		
		StateSteps wait2State = new StateSteps(new OddjobLookup(oddjob).lookup(
				"wait1", Stateful.class));
		wait2State.startCheck(JobState.READY, JobState.EXECUTING);
		
		Thread t = new Thread(oddjob);
		t.start();
	
		wait2State.checkWait();
		
		StateSteps oddjobState = new StateSteps(oddjob);
		oddjobState.startCheck(JobState.EXECUTING, JobState.COMPLETE);
		
		oddjob.stop();

		oddjobState.checkWait();
		
		oddjob.destroy();
		
	}
	
}
