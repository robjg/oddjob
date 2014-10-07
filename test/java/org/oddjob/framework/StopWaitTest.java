package org.oddjob.framework;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Stateful;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.JobState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.StateListener;
import org.oddjob.util.OddjobLockedException;

public class StopWaitTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(StopWaitTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("-------------------  " + getName() + "  ----------------");
	}
	
	private class OurStateful implements Stateful {
		
		JobStateHandler jobStateHandler = new JobStateHandler(this);

		Set<StateListener> listeners = Collections.synchronizedSet(
				new HashSet<StateListener>());
		
		void fireState(final JobState state) throws OddjobLockedException {
			jobStateHandler.tryToWhen(new IsAnyState(), 
					new Runnable() {
				public void run() {
					jobStateHandler.setState(state);
					jobStateHandler.fireEvent();
				}
			});
		}
		
		@Override
		public void addStateListener(StateListener listener) {
			jobStateHandler.addStateListener(listener);
			listeners.add(listener);			
		}
		
		@Override
		public StateEvent lastStateEvent() {
			return jobStateHandler.lastStateEvent();
		}
		
		@Override
		public void removeStateListener(StateListener listener) {
			jobStateHandler.removeStateListener(listener);
			listeners.remove(listener);
		}
	}
	
	public void testStopWaitOnReady() throws FailedToStopException {
		
		OurStateful stateful = new OurStateful();
		
		new StopWait(stateful).run();
		
		assertEquals(0, stateful.listeners.size());	
	}
	
	public void testFailedTostop() throws OddjobLockedException {
		
		final OurStateful stateful = new OurStateful();
		stateful.fireState(JobState.EXECUTING);
		
		try {
			new StopWait(stateful, 10).run();
			fail("Should throw excption.");
		}
		catch (FailedToStopException e) {
			// expected
		}
		
		assertEquals(0, stateful.listeners.size());
	
	}
	
	public void testSlowToStop() throws OddjobLockedException, FailedToStopException {
		
		final OurStateful stateful = new OurStateful();
		stateful.fireState(JobState.EXECUTING);
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (stateful.listeners.isEmpty()) {
					logger.info("Nothing listening yet...");
					try {
						Thread.sleep(10);
						
					} 
					catch (InterruptedException e) {
						throw new RuntimeException("Unexpected!");
					}
				}
				try {
					logger.info("Setting state COMPLETE");
					stateful.fireState(JobState.COMPLETE);
					logger.info("State set to COMPLETE");
				} catch (OddjobLockedException e) {
					throw new RuntimeException("Unexpected!");
				}				
			}
		});
		t.start();
		
		new StopWait(stateful).run();
		
		assertEquals(0, stateful.listeners.size());
	
	}
	
	public void testStatefulDestroyed() throws OddjobLockedException, FailedToStopException {
		
		final OurStateful stateful = new OurStateful();
		stateful.fireState(JobState.EXECUTING);
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (stateful.listeners.isEmpty()) {
					logger.info("Nothing listening yet...");
					try {
						Thread.sleep(10);
						
					} 
					catch (InterruptedException e) {
						throw new RuntimeException("Unexpected!");
					}
				}
				try {
					logger.info("Setting state DESTROYED");
					stateful.fireState(JobState.DESTROYED);
					logger.info("State set to DESTROYED");
				} catch (OddjobLockedException e) {
					throw new RuntimeException("Unexpected!");
				}				
			}
		});
		t.start();
		
		new StopWait(stateful).run();
		
		assertEquals(0, stateful.listeners.size());
	
	}
}
