package org.oddjob.jobs;

import org.junit.Test;

import java.io.IOException;

import org.oddjob.OjTestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.jobs.GrabJob.LoosingAction;
import org.oddjob.scheduling.Keeper;
import org.oddjob.scheduling.LoosingOutcome;
import org.oddjob.scheduling.Outcome;
import org.oddjob.scheduling.WinningOutcome;
import org.oddjob.state.FlagState;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.tools.OddjobTestHelper;

public class GrabJobTest extends OjTestCase {

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
	
   @Test
	public void testAsWinner() throws IOException, ClassNotFoundException {
		
		GrabJob test = new GrabJob();
		
		FlagState flag = new FlagState(JobState.COMPLETE);
		
		WinnerKeeper keeper = new WinnerKeeper();
		
		test.setKeeper(keeper);
		test.setIdentifier("me");
		test.setJob(flag);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, flag.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		
		assertEquals("me", test.getWinner());
		assertEquals(true, keeper.complete);
		
		GrabJob copy = OddjobTestHelper.copy(test);
		
		assertEquals(JobState.COMPLETE, copy.lastStateEvent().getState());
		assertEquals("me", copy.getWinner());
	}
	
	class LooserKeeper implements Keeper {
	
		StateListener listener;
		
		@Override
		public Outcome grab(String ourIdentifier, Object ourInstance) {
			return new LoosingOutcome() {
				
				@Override
				public void removeStateListener(StateListener l) {
					if (listener == null) {
						throw new IllegalStateException("No Listener.");
					}
					assertEquals(listener, l);
					listener = null;
				}
				
				@Override
				public void addStateListener(StateListener l) {
					if (listener != null) {
						throw new IllegalStateException("Listener already set.");
					}
					listener = l;
				}

				@Override
				public StateEvent lastStateEvent() {
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
	
	
   @Test
	public void testNotWinner() {
		
		GrabJob test = new GrabJob();
		
		FlagState flag = new FlagState(JobState.COMPLETE);
		
		LooserKeeper keeper = new LooserKeeper();
		
		test.setKeeper(keeper);
		test.setIdentifier("me");
		test.setJob(flag);
		test.setOnLoosing(LoosingAction.WAIT);
		test.run();
		
		assertEquals(JobState.READY, flag.lastStateEvent().getState());
		assertEquals(JobState.EXECUTING, test.lastStateEvent().getState());
		
		keeper.listener.jobStateChange(new StateEvent(flag, JobState.COMPLETE));
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		assertEquals("complete", OddjobTestHelper.getIconId(test));
		
		assertEquals("not you", test.getWinner());
	}
	
   @Test
	public void testStopAsLooser() throws FailedToStopException {
		
		GrabJob test = new GrabJob();
		
		FlagState flag = new FlagState(JobState.COMPLETE);
		
		LooserKeeper keeper = new LooserKeeper();
		
		test.setKeeper(keeper);
		test.setIdentifier("me");
		test.setJob(flag);
		test.setOnLoosing(LoosingAction.WAIT);
		test.run();
		
		assertEquals(JobState.READY, flag.lastStateEvent().getState());
		assertEquals(JobState.EXECUTING, test.lastStateEvent().getState());
		
		test.stop();

		assertEquals(JobState.INCOMPLETE, test.lastStateEvent().getState());
		assertNull(keeper.listener);
		
		assertEquals("not you", test.getWinner());
	}
	
   @Test
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
		checkExecuting.setState(new IsStoppable());
		checkExecuting.run();
		
		test.stop();

		t.join();
		
		assertEquals(JobState.COMPLETE, wait.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
	}
	
   @Test
	public void testSerialize() throws IOException, ClassNotFoundException {
		
		GrabJob test = new GrabJob();
		
		FlagState flag = new FlagState(JobState.COMPLETE);
		
		WinnerKeeper keeper = new WinnerKeeper();
		
		test.setKeeper(keeper);
		test.setIdentifier("me");
		test.setJob(flag);
		
		test.run();
		
		assertEquals(JobState.COMPLETE, flag.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		
		GrabJob copy = OddjobTestHelper.copy(test);

		assertEquals(JobState.COMPLETE, copy.lastStateEvent().getState());
		assertEquals("me", test.getWinner());
		
		copy.setJob(flag);
		
		copy.hardReset();
		
		assertEquals(JobState.READY, flag.lastStateEvent().getState());
		assertEquals(JobState.READY, copy.lastStateEvent().getState());
		assertNull(copy.getWinner());
		
		copy.setKeeper(new WinnerKeeper());
		
		copy.run();
		
		assertEquals(JobState.COMPLETE, flag.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, copy.lastStateEvent().getState());
		assertEquals("me", copy.getWinner());
	}
}
