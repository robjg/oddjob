package org.oddjob.jobs.structural;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.framework.SimpleJob;
import org.oddjob.scheduling.MockScheduledExecutorService;
import org.oddjob.scheduling.MockScheduledFuture;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.tools.StateSteps;

public class ParallelJobInStepsTest extends TestCase {

	private class ManualExecutor1 extends MockScheduledExecutorService {

		private Runnable runnable;
		
		public Future<?> submit(Runnable runnable) {
			if (this.runnable != null) {
				throw new IllegalStateException();
			}
			this.runnable = runnable;
			return new MockScheduledFuture<Void>();
		}
	}
	
	public void testStatesExecutingAndCompletingOneJob() {
		
		FlagState job1 = new FlagState(JobState.COMPLETE);

		ManualExecutor1 executor = new ManualExecutor1();
		
		ParallelJob test = new ParallelJob();
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE);
		
		test.setExecutorService(executor);

		test.setJobs(0, job1);
		
		test.run();
	
		steps.checkNow();
		
		assertNotNull(executor.runnable);
		
		steps.startCheck(ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		executor.runnable.run();
		
		steps.checkNow();
		
		steps.startCheck(ParentState.COMPLETE, ParentState.DESTROYED);
		
		test.destroy();
		
		steps.checkNow();
	}
	
	private class ManualExecutor2 extends MockScheduledExecutorService {

		boolean cancelled;
		
		public Future<?> submit(Runnable runnable) {
			return new MockScheduledFuture<Void>() {
				@Override
				public boolean cancel(boolean mayInterruptIfRunning) {
					assertFalse(mayInterruptIfRunning);
					cancelled = true;
					return true;
				}
			};
		}
	}
	
	private class CaptureStoppedJob extends SimpleJob
	implements Stoppable {

		@Override
		protected int execute() throws Throwable {
			throw new RuntimeException("Unexpected");
		}
	}
	

	public void testStatesWhenStoppingJobThatHasntExecuted() throws FailedToStopException {
		
		CaptureStoppedJob job1 = new CaptureStoppedJob();

		ManualExecutor2 executor = new ManualExecutor2();
		
		ParallelJob test = new ParallelJob();
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE);
		
		test.setExecutorService(executor);

		test.setJobs(0, job1);
		
		test.run();
	
		steps.checkNow();
		
		steps.startCheck(ParentState.ACTIVE,
				ParentState.READY);
		
		test.stop();
		
		steps.checkNow();

		assertEquals(false, job1.isStop());
		
		assertEquals(true, executor.cancelled);
		
		steps.startCheck(ParentState.READY, ParentState.DESTROYED);
		
		test.destroy();
		
		steps.checkNow();
	}
	
	public void testStatesWhenDestroyedWithoutStoppingJobThatHasntExecuted() {
		
		CaptureStoppedJob job1 = new CaptureStoppedJob();

		ManualExecutor2 executor = new ManualExecutor2();
		
		ParallelJob test = new ParallelJob();
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE);
		
		test.setExecutorService(executor);

		test.setJobs(0, job1);
		
		test.run();
	
		steps.checkNow();
		
		steps.startCheck(ParentState.ACTIVE,
				ParentState.DESTROYED);

		test.destroy();
				
		assertEquals(false, job1.isStop());
		
		assertEquals(true, executor.cancelled);
		
		steps.checkNow();
	}
	
	private class ManualExecutor3 extends MockScheduledExecutorService {

		boolean cancelled;
		
		Runnable runnable;
		
		public Future<?> submit(Runnable runnable) {
			this.runnable = runnable;
			return new MockScheduledFuture<Void>() {
				@Override
				public boolean cancel(boolean mayInterruptIfRunning) {
					assertFalse(mayInterruptIfRunning);
					cancelled = true;
					return false;
				}
			};
		}
	}
	
	private class AsyncJob implements Stateful, Runnable, Stoppable {
		
		private final JobStateHandler stateHandler = 
				new JobStateHandler(this);

		private void sendEvent(final JobState jobState) {
			stateHandler.callLocked(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					stateHandler.setState(jobState);
					stateHandler.fireEvent();
					return null;
				}
			});
		}
		
		@Override
		public void run() {
			sendEvent(JobState.EXECUTING);
		}

		@Override
		public void addStateListener(StateListener listener)
				throws JobDestroyedException {
			stateHandler.addStateListener(listener);
		}

		@Override
		public void removeStateListener(StateListener listener) {
			stateHandler.removeStateListener(listener);
		}

		@Override
		public void stop() throws FailedToStopException {
			sendEvent(JobState.COMPLETE);
		}
		
		@Override
		public StateEvent lastStateEvent() {
			return stateHandler.lastStateEvent();
		}
	}
	
	public void testStatesWhenStoppingAsyncJobThatIsStillExecuting() throws FailedToStopException {
		
		AsyncJob job1 = new AsyncJob();

		ManualExecutor3 executor = new ManualExecutor3();
		
		ParallelJob test = new ParallelJob();
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE);
		
		test.setExecutorService(executor);

		test.setJobs(0, job1);
		
		test.run();
	
		executor.runnable.run();
		
		steps.checkNow();
		
		steps.startCheck(ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		test.stop();
		
		steps.checkNow();

		assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
		
		assertEquals(true, executor.cancelled);
		
		steps.startCheck(ParentState.COMPLETE, ParentState.DESTROYED);
		
		test.destroy();
		
		steps.checkNow();
	}
	
	public void testStatesWhenDestroyedWithoutStoppingAsyncJobThatIsStillExecuting() {
		
		AsyncJob job1 = new AsyncJob();

		ManualExecutor3 executor = new ManualExecutor3();
		
		ParallelJob test = new ParallelJob();
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE);
		
		test.setExecutorService(executor);

		test.setJobs(0, job1);
		
		test.run();
	
		executor.runnable.run();
		
		steps.checkNow();
		
		steps.startCheck(ParentState.ACTIVE,
				ParentState.DESTROYED);
		
		test.destroy();
		
		steps.checkNow();

		assertEquals(JobState.EXECUTING, job1.lastStateEvent().getState());
		
		assertEquals(true, executor.cancelled);
		
	}
}
