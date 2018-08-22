package org.oddjob.state;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import org.oddjob.OjTestCase;

import org.oddjob.MockStateful;
import org.oddjob.util.OddjobLockedException;

public class StateSupportLockTest extends OjTestCase {

//	private static final Logger logger = LoggerFactory.getLogger(StateSupportLockTest.class);
	
	class IsLocked implements Runnable {
		boolean locked;
		
		final JobStateHandler state;
		
		IsLocked(JobStateHandler state) {
			this.state = state;
		}
		
		public void run() {
			try {
				boolean condition = state.tryToWhen(new IsAnyState(), 
						new Runnable() {
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
	
   @Test
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
					
					test.setState(JobState.COMPLETE);
					test.fireEvent();
				}
			});
					
		assertTrue(succeeded);
		
		Thread t = new Thread(check);
		
		t.start();
		t.join();
		
		assertFalse(check.locked);
		
		assertEquals(JobState.COMPLETE, test.getState());
	}
	
   @Test
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
					
					test.setState(JobState.COMPLETE);
					test.fireEvent();
				}
			});
					
		assertTrue(succeeded);
		
		Thread t = new Thread(check);
		
		t.start();
		t.join();
		
		assertFalse(check.locked);
		
		assertEquals(JobState.COMPLETE, test.getState());
	}
	
	
   @Test
	public void testInturruptedFlag() {
		
		JobStateHandler test = new JobStateHandler(new MockStateful());
		
		Thread.currentThread().interrupt();
		
		final AtomicBoolean ran = new AtomicBoolean();
		
		test.waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				ran.set(true);
			}
		});
		
		assertTrue(ran.get());
		assertTrue(Thread.interrupted());		
	}
	
//	public void testTimeout() throws InterruptedException, BrokenBarrierException {
//		
//		String timeout = System.getProperty(
//				StateHandler.LOCK_TIMEOUT_PROPERTY);
//		
//		System.setProperty(StateHandler.LOCK_TIMEOUT_PROPERTY, "100");
//		
//		
//		final JobStateHandler test = new JobStateHandler(new MockStateful());
//		
//		final CyclicBarrier barrier = new CyclicBarrier(2);
//		
//		Thread t = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				try {
//					test.tryToWhen(new IsAnyState(), new Runnable() {
//						
//						@Override
//						public void run() {
//							try {
//								barrier.await();
//								
//								barrier.reset();
//								
//								barrier.await();
//							} catch (InterruptedException e) {
//								throw new RuntimeException(e);
//							} catch (BrokenBarrierException e) {
//								throw new RuntimeException(e);
//							}
//						}
//					});
//				} catch (OddjobLockedException e) {
//					throw new RuntimeException(e);
//				}
//					
//			}
//		});
//		
//		t.start();
//		
//		barrier.await();
//		
//		try {
//			test.waitToWhen(new IsAnyState(), new Runnable() {
//				@Override
//				public void run() {
//					fail("Unexpected.");
//				}
//			});
//			
//			fail("Should timeout.");
//		}
//		catch (OddjobLockTimeoutException e) {
//			// expected.
//			logger.info(e.toString());
//		}
//		
//		barrier.await();
//		
//		t.join();
//		
//		if (timeout != null) {
//			System.setProperty(
//					StateHandler.LOCK_TIMEOUT_PROPERTY, timeout);
//		}
//	}
}
