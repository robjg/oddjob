package org.oddjob.jobs.structural;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.MockStateful;
import org.oddjob.Oddjob;
import org.oddjob.OddjobComponentResolver;
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
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

public class ParallelJobTest extends TestCase {

	public void testThreeJobs() {
	
		FlagState job1 = new FlagState(JobState.COMPLETE);
		FlagState job2 = new FlagState(JobState.COMPLETE);
		FlagState job3 = new FlagState(JobState.COMPLETE);

		DefaultExecutors defaultServices = new DefaultExecutors();
		
		ParallelJob test = new ParallelJob();
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.COMPLETE);
		
		test.setExecutorService(defaultServices.getPoolExecutor());

		test.setJobs(0, job1);
		test.setJobs(1, job2);
		test.setJobs(2, job3);
		
		test.run();
	
		assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
		
		steps.checkNow();
		
		steps.startCheck(ParentState.COMPLETE, ParentState.READY);
		
		test.hardReset();
		
		assertEquals(JobState.READY, job1.lastStateEvent().getState());
		assertEquals(JobState.READY, job2.lastStateEvent().getState());
		assertEquals(JobState.READY, job3.lastStateEvent().getState());
		
		steps.checkNow();
		
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.COMPLETE);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());
		
		steps.checkNow();
	
		defaultServices.stop();
	}
	
	public void testThrottledExecution() {
		
		FlagState job1 = new FlagState(JobState.COMPLETE);
		FlagState job2 = new FlagState(JobState.COMPLETE);
		FlagState job3 = new FlagState(JobState.COMPLETE);

		DefaultExecutors defaultServices = new DefaultExecutors();
		defaultServices.setPoolSize(1);
		
		ParallelJob test = new ParallelJob();
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.COMPLETE);
		
		test.setExecutorService(defaultServices.getPoolExecutor());

		test.setJobs(0, job1);
		test.setJobs(0, job2);
		test.setJobs(0, job3);
		
		test.run();
	
		assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
		
		steps.checkNow();		
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
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		test.destroy();
	}
	
	private class DestroyJob extends MockStateful 
	implements Runnable {
		
		JobStateHandler handler = new JobStateHandler(this);
		
		public void addStateListener(StateListener listener) {
			handler.addStateListener(listener);
		}
		public void removeStateListener(StateListener listener) {
			handler.removeStateListener(listener);
		}
		
		public void run() {
			handler.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
					handler.setState(JobState.COMPLETE);
					handler.fireEvent();
				}
			});
		}
		
		void destroy() {
			handler.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
					handler.setState(JobState.DESTROYED);
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
		
		StateEvent event = test.lastStateEvent();
		assertEquals(ParentState.COMPLETE, event.getState());
		
		destroy.destroy();
		
		assertEquals(event, test.lastStateEvent());
		
	}
	
	private class RecordingStateListener implements StateListener {
		
		List<StateEvent> events = new ArrayList<StateEvent>();
		
		public void jobStateChange(StateEvent event) {
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
		test.addStateListener(recorder);
		
		assertEquals(1, recorder.events.size());
		assertEquals(ParentState.READY, recorder.events.get(0).getState());
		
		test.setExecutorService(defaultServices.getPoolExecutor());

		test.setJobs(0, job1);
		test.setJobs(1, job2);
		test.setJobs(2, job3);
		
		test.run();

		assertEquals(3, recorder.events.size());
		assertEquals(ParentState.EXECUTING, recorder.events.get(1).getState());
		assertEquals(ParentState.COMPLETE, recorder.events.get(2).getState());
		
		test.hardReset();
		
		assertEquals(4, recorder.events.size());
		assertEquals(ParentState.READY, recorder.events.get(3).getState());
		
		test.run();
		
		assertEquals(6, recorder.events.size());
		assertEquals(ParentState.EXECUTING, recorder.events.get(4).getState());
		assertEquals(ParentState.COMPLETE, recorder.events.get(5).getState());
	
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
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
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

		oddjobState.checkNow();
		
		oddjob.destroy();
		
	}
	
	public static class MyService {
		
		public void start() {}
		public void stop() {}
	}
	
	public void testParallelServices() throws FailedToStopException {
		
		DefaultExecutors defaultServices = new DefaultExecutors();
		
		ParallelJob test = new ParallelJob();
		
		test.setExecutorService(defaultServices.getPoolExecutor());
		
		Object service1 = new OddjobComponentResolver().resolve(
				new MyService(), null);
		Object service2 = new OddjobComponentResolver().resolve(
				new MyService(), null);
		
		test.setJobs(0, (Runnable) service1);
		test.setJobs(1, (Runnable) service2);
		
		StateSteps steps = new StateSteps(test);
		
		steps.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.ACTIVE);
		
		test.run();
		
		steps.checkNow();
		
		steps.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
		
		test.stop();

		steps.checkNow();
		
		defaultServices.stop();
	}
	
}
