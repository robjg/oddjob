package org.oddjob.state;

import org.oddjob.MockStateful;
import org.oddjob.util.OddjobLockedException;

import junit.framework.TestCase;

public class StateSupportLockTest extends TestCase {

	class IsLocked implements Runnable {
		boolean locked;
		
		final JobStateHandler state;
		
		IsLocked(JobStateHandler state) {
			this.state = state;
		}
		
		public void run() {
			try {
				boolean condition = state.tryToWhen(new StateCondition() {
					public boolean test(JobState state) {
						return true;
					}
				}, new Runnable() {
					public void run() {
					}
				});
				assertTrue(condition);
				locked = false;
			} catch (OddjobLockedException e) {
				locked = true;
			}
		}
	}
	
	public void testAsIfExecuting() throws InterruptedException {
		
		final JobStateHandler test = new JobStateHandler(new MockStateful());
		
		final IsLocked check = new IsLocked(test);
		
		boolean succeeded = test.waitToWhen(new IsExecutable(),
			new Runnable() {
				public void run() {
					Thread t;
					
					
					t = new Thread(check);
					
					t.start();
					try {
						t.join();
					} catch (InterruptedException e) {
						fail("Unexpected");
					}
					
					assertTrue(check.locked);
					
					test.setJobState(JobState.COMPLETE);
					test.fireEvent();
				}
			});
					
		assertTrue(succeeded);
		
		Thread t = new Thread(check);
		
		t.start();
		t.join();
		
		assertFalse(check.locked);
		
		assertEquals(JobState.COMPLETE, test.getJobState());
	}
	
	public void testWaitFor() throws InterruptedException {
		
		final JobStateHandler test = new JobStateHandler(new MockStateful());
		
		final IsLocked check = new IsLocked(test);
		
		boolean succeeded = test.waitToWhen(new IsExecutable(),
			new Runnable() {
				public void run() {
					Thread t;
					
					
					t = new Thread(check);
					
					t.start();
					try {
						t.join();
					} catch (InterruptedException e) {
						fail("Unexpected");
					}
					
					assertTrue(check.locked);
					
					test.setJobState(JobState.COMPLETE);
					test.fireEvent();
				}
			});
					
		assertTrue(succeeded);
		
		Thread t = new Thread(check);
		
		t.start();
		t.join();
		
		assertFalse(check.locked);
		
		assertEquals(JobState.COMPLETE, test.getJobState());
	}
	
	
	public void testInturruptedFlag() {
		
		final boolean[] result = new boolean[1];
		
		JobStateHandler test = new JobStateHandler(new MockStateful());
		
		Thread.currentThread().interrupt();
		
		test.waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				result[0] = Thread.currentThread().isInterrupted();
			}
		});
		
		assertTrue(Thread.interrupted());
		assertTrue(result[0]);
	}
}
