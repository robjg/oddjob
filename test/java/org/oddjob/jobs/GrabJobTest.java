package org.oddjob.jobs;

import java.io.IOException;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.jobs.GrabJob.LoosingAction;
import org.oddjob.scheduling.Keeper;
import org.oddjob.scheduling.LoosingOutcome;
import org.oddjob.scheduling.Outcome;
import org.oddjob.scheduling.WinningOutcome;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

public class GrabJobTest extends TestCase {

	class WinnerKeeper implements Keeper {
		
		boolean complete;
		
		@Override
		public Outcome grab(final String name, Object instance) {
			
			return new WinningOutcome() {
				
				@Override
				public boolean isWon() {
					return true;
				}
				
				@Override
				public String getWinner() {
					return name;
				}
				
				@Override
				public void complete() {
					complete = true;
				}
			};
		}
		
	}
	
	public void testAsWinner() throws IOException, ClassNotFoundException {
		
		GrabJob test = new GrabJob();
		
		FlagState flag = new FlagState(JobState.COMPLETE);
		
		WinnerKeeper keeper = new WinnerKeeper();
		
		test.setKeeper(keeper);
		test.setIdentifier("me");
		test.setJob(flag);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, flag.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		assertEquals("me", test.getWinner());
		assertEquals(true, keeper.complete);
		
		GrabJob copy = Helper.copy(test);
		
		assertEquals(JobState.COMPLETE, copy.lastJobStateEvent().getJobState());
		assertEquals("me", copy.getWinner());
	}
	
	class LooserKeeper implements Keeper {
	
		JobStateListener listener;
		
		@Override
		public Outcome grab(String ourIdentifier, Object ourInstance) {
			return new LoosingOutcome() {
				
				@Override
				public void removeJobStateListener(JobStateListener l) {
					if (listener == null) {
						throw new IllegalStateException("No Listener.");
					}
					assertEquals(listener, l);
					listener = null;
				}
				
				@Override
				public void addJobStateListener(JobStateListener l) {
					if (listener != null) {
						throw new IllegalStateException("Listener already set.");
					}
					listener = l;
				}

				@Override
				public JobStateEvent lastJobStateEvent() {
					throw new RuntimeException("Unexpected.");
				}
				
				@Override
				public boolean isWon() {
					return false;
				}
				
				@Override
				public String getWinner() {
					return "not you";
				}
			};
		}		
	}	
	
	
	public void testNotWinner() {
		
		GrabJob test = new GrabJob();
		
		FlagState flag = new FlagState(JobState.COMPLETE);
		
		LooserKeeper keeper = new LooserKeeper();
		
		test.setKeeper(keeper);
		test.setIdentifier("me");
		test.setJob(flag);
		test.setOnLoosing(LoosingAction.WAIT);
		test.run();
		
		assertEquals(JobState.READY, flag.lastJobStateEvent().getJobState());
		assertEquals(JobState.EXECUTING, test.lastJobStateEvent().getJobState());
		
		keeper.listener.jobStateChange(new JobStateEvent(flag, JobState.COMPLETE));
		
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		assertEquals("complete", Helper.getIconId(test));
		
		assertEquals("not you", test.getWinner());
	}
	
	public void testStopAsLooser() throws FailedToStopException {
		
		GrabJob test = new GrabJob();
		
		FlagState flag = new FlagState(JobState.COMPLETE);
		
		LooserKeeper keeper = new LooserKeeper();
		
		test.setKeeper(keeper);
		test.setIdentifier("me");
		test.setJob(flag);
		test.setOnLoosing(LoosingAction.WAIT);
		test.run();
		
		assertEquals(JobState.READY, flag.lastJobStateEvent().getJobState());
		assertEquals(JobState.EXECUTING, test.lastJobStateEvent().getJobState());
		
		test.stop();

		assertEquals(JobState.INCOMPLETE, test.lastJobStateEvent().getJobState());
		assertNull(keeper.listener);
		
		assertEquals("not you", test.getWinner());
	}
	
	public void testStopAsWinner() throws FailedToStopException, InterruptedException {
		
		GrabJob test = new GrabJob();
		
		WaitJob wait = new WaitJob();
		
		WinnerKeeper keeper = new WinnerKeeper();
		
		test.setKeeper(keeper);
		test.setIdentifier("me");
		test.setJob(wait);
		
		Thread t = new Thread(test);
		t.start();
		
		WaitJob checkExecuting = new WaitJob();
		checkExecuting.setFor(wait);
		checkExecuting.setState("EXECUTING");
		checkExecuting.run();
		
		test.stop();

		t.join();
		
		assertEquals(JobState.COMPLETE, wait.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
	}
	
	public void testSerialize() throws IOException, ClassNotFoundException {
		
		GrabJob test = new GrabJob();
		
		FlagState flag = new FlagState(JobState.COMPLETE);
		
		WinnerKeeper keeper = new WinnerKeeper();
		
		test.setKeeper(keeper);
		test.setIdentifier("me");
		test.setJob(flag);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, flag.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		GrabJob copy = Helper.copy(test);

		assertEquals(JobState.COMPLETE, copy.lastJobStateEvent().getJobState());
		assertEquals("me", test.getWinner());
		
		copy.setJob(flag);
		
		copy.hardReset();
		
		assertEquals(JobState.READY, flag.lastJobStateEvent().getJobState());
		assertEquals(JobState.READY, copy.lastJobStateEvent().getJobState());
		assertNull(copy.getWinner());
		
		copy.setKeeper(new WinnerKeeper());
		
		copy.run();
		
		assertEquals(JobState.COMPLETE, flag.lastJobStateEvent().getJobState());
		assertEquals(JobState.COMPLETE, copy.lastJobStateEvent().getJobState());
		assertEquals("me", copy.getWinner());
	}
}
