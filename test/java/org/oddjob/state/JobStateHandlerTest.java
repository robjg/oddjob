/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicBoolean;

import org.oddjob.OjTestCase;

import org.oddjob.MockStateful;
import org.oddjob.Stateful;

/**
 * 
 */
public class JobStateHandlerTest extends OjTestCase {

		
	private void setState(final JobStateHandler handler, 
			final JobState state) {
		boolean ran = handler.waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				handler.setState(state);
				handler.fireEvent();
			}
		});
		assertTrue(ran);
	}
	
	private void setException(final JobStateHandler handler, 
			final Exception e) {
		boolean ran = handler.waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				handler.setStateException(JobState.EXCEPTION, e);
				handler.fireEvent();
			}
		});
		assertTrue(ran);
	}
	
   @Test
	public void testAllStates() {
		
		JobStateHandler test = new JobStateHandler(new MockStateful());
		
		assertEquals(JobState.READY, test.getState());
		assertEquals(JobState.READY, test.lastStateEvent().getState());
		
		setState(test, JobState.EXECUTING);
		
		assertEquals(JobState.EXECUTING, test.getState());
		assertEquals(JobState.EXECUTING, test.lastStateEvent().getState());

		setState(test, JobState.COMPLETE);
		
		assertEquals(JobState.COMPLETE, test.getState());
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		
		setState(test, JobState.INCOMPLETE);
		
		assertEquals(JobState.INCOMPLETE, test.getState());
		assertEquals(JobState.INCOMPLETE, test.lastStateEvent().getState());

		setException(test, new Exception());
		
		assertEquals(JobState.EXCEPTION, test.getState());
		assertEquals(JobState.EXCEPTION, test.lastStateEvent().getState());

		setState( test, JobState.READY);
		
		assertEquals(JobState.READY, test.getState());
		assertEquals(JobState.READY, test.lastStateEvent().getState());
	}

	private class RecordingStateListener implements StateListener {
		
		List<StateEvent> events = new ArrayList<StateEvent>();
		
		public synchronized void jobStateChange(StateEvent event) {
			events.add(event);
		}
	}
	
   @Test
	public void testListenersNotified() {

		Stateful source = new MockStateful();
		JobStateHandler test = new JobStateHandler(source);

		RecordingStateListener l = new RecordingStateListener();
		test.addStateListener(l);
		
		assertEquals(1, l.events.size());
		assertEquals(JobState.READY, l.events.get(0).getState());
		assertEquals(source, l.events.get(0).getSource());

		setState(test, JobState.EXECUTING);
		
		assertEquals(2, l.events.size());
		assertEquals(JobState.EXECUTING, l.events.get(1).getState());

		setState(test, JobState.COMPLETE);
		
		assertEquals(3, l.events.size());
		assertEquals(JobState.COMPLETE, l.events.get(2).getState());
		
		setState(test, JobState.INCOMPLETE);
		
		assertEquals(4, l.events.size());
		assertEquals(JobState.INCOMPLETE, l.events.get(3).getState());

		Exception e = new Exception();
		setException(test, e);
		
		assertEquals(5, l.events.size());
		assertEquals(JobState.EXCEPTION, l.events.get(4).getState());
		assertEquals(e, l.events.get(4).getException());

		setState(test, JobState.READY);
		
		assertEquals(6, l.events.size());
		assertEquals(JobState.READY, l.events.get(5).getState());
		
		setState(test, JobState.DESTROYED);
		
		assertEquals(7, l.events.size());
		assertEquals(JobState.DESTROYED, l.events.get(6).getState());
	}

   @Test
	public void testDuplicateEventsNotified() {

		Stateful source = new MockStateful();
		JobStateHandler test = new JobStateHandler(source);

		RecordingStateListener l = new RecordingStateListener();
		test.addStateListener(l);
		
		assertEquals(1, l.events.size());
		assertEquals(JobState.READY, l.events.get(0).getState());
		assertEquals(source, l.events.get(0).getSource());

		setState(test, JobState.READY);
		
		assertEquals(2, l.events.size());
		
		setState(test, JobState.EXECUTING);
		
		assertEquals(3, l.events.size());
		
		setState(test, JobState.EXECUTING);
		
		assertEquals(4, l.events.size());
		assertEquals(JobState.EXECUTING, l.events.get(3).getState());

		setState(test, JobState.COMPLETE);
		
		assertEquals(5, l.events.size());
		assertEquals(JobState.COMPLETE, l.events.get(4).getState());
	}
	
   @Test
	public void testManyListeners() throws Exception {
		
		RecordingStateListener l1 = new RecordingStateListener();
		RecordingStateListener l2 = new RecordingStateListener();
		RecordingStateListener l3 = new RecordingStateListener();
		
		final JobStateHandler test = new JobStateHandler(new MockStateful());
		Thread t = new Thread(new Runnable() {
			public void run() {
				setState(test, JobState.COMPLETE);
			}
		});
		
		test.addStateListener(l1);
		test.addStateListener(l2);
		test.addStateListener(l3);
		
		t.start();
		
		t.join();
		
		assertEquals(2, l1.events.size());
		assertEquals(2, l2.events.size());
		assertEquals(2, l3.events.size());
		
		assertEquals(JobState.COMPLETE, l1.events.get(1).getState());
		assertEquals(JobState.COMPLETE, l2.events.get(1).getState());
		assertEquals(JobState.COMPLETE, l3.events.get(1).getState());
	}
	
	
   @Test
	public void testListenerConcurrentModification() {
		
		final JobStateHandler test = new JobStateHandler(new MockStateful());

		test.addStateListener(new StateListener() {
			public void jobStateChange(StateEvent event) {
				if (event.getState() == JobState.COMPLETE) {
					test.removeStateListener(this);
				}
			}
		});
		
		assertEquals(1, test.listenerCount());
		
		test.waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				test.setState(JobState.COMPLETE);
				test.fireEvent();
			}
		});
		
		assertEquals(0, test.listenerCount());
	}
	
   @Test
	public void testThatAttemptsToChangeState() {
		
		final JobStateHandler test = new JobStateHandler(new MockStateful());
		
		StateListener listener = new StateListener() {
			
			@Override
			public void jobStateChange(StateEvent event) {
				test.waitToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						test.setState(JobState.COMPLETE);
						test.fireEvent();
					}
				});
			}
		};
		
		try {
			test.addStateListener(listener);
			fail("Expected to fail.");
		}
		catch (IllegalStateException e) {
			// expected
		}

		final AtomicBoolean failed = new AtomicBoolean();
		
		StateListener listener2 = new StateListener() {
			
			@Override
			public void jobStateChange(final StateEvent event) {
				
				if (JobState.INCOMPLETE == event.getState()) {
					try {
						test.setState(JobState.COMPLETE);
						test.fireEvent();
					}
					catch (IllegalStateException e) {
						failed.set(true);
					}
				}
			}
		};

		test.addStateListener(listener2);
		
		test.waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				test.setState(JobState.INCOMPLETE);
				test.fireEvent();
			}
		});
		
		assertTrue(failed.get());
		assertEquals(JobState.INCOMPLETE, test.lastStateEvent().getState());
	}
	
   @Test
	public void testListenerNotificationOrder() throws InterruptedException {
		
		final JobStateHandler test = new JobStateHandler(new MockStateful());
		
		final Exchanger<Void> exchanger = new Exchanger<Void>();
		
		test.addStateListener(new StateListener() {
			
			@Override
			public void jobStateChange(StateEvent event) {
				try {
					if (event.getState() == JobState.COMPLETE) {
						exchanger.exchange(null);
						exchanger.exchange(null);
					}
				} catch (InterruptedException e) {
					throw new RuntimeException("Unexpected", e);
				}
			}
		});
		
		final List<State> events = new ArrayList<State>();
		
		StateListener listener = new StateListener() {
			
			@Override
			public void jobStateChange(StateEvent event) {
				events.add(event.getState());
			}
		};
		
		new Thread() {
			@Override
			public void run() {
				test.waitToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						test.setState(JobState.COMPLETE);
						test.fireEvent();
					}
				});
			}
		}.start();

		exchanger.exchange(null);
		exchanger.exchange(null);
				
		// able to get event before other listener complete.
		test.addStateListener(listener);
		
		assertEquals(JobState.COMPLETE, events.get(0));
		assertEquals(1, events.size());
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
	}
	
   @Test
	public void testSleep() throws InterruptedException {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		final JobStateHandler test = new JobStateHandler(new MockStateful());
		
		final List<State> events = new ArrayList<State>();
		
		StateListener listener = new StateListener() {
			
			@Override
			public void jobStateChange(StateEvent event) {
				events.add(event.getState());
			}
		};
		
		test.addStateListener(listener);
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				test.waitToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						latch.countDown();
						try {
							test.sleep(0);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						test.setState(JobState.COMPLETE);
						test.fireEvent();
					}
				});
			}
		});
		
		Thread t2 = new Thread() {
			@Override
			public void run() {
				try {
					latch.await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				test.waitToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						test.setState(JobState.INCOMPLETE);
						test.fireEvent();
						test.wake();
					}
				});
			}
		};

		t1.start();
		t2.start();
		
		t1.join();
		t2.join();
		
		assertEquals(JobState.READY, events.get(0));
		assertEquals(JobState.INCOMPLETE, events.get(1));
		assertEquals(JobState.COMPLETE, events.get(2));
		
		assertEquals(3, events.size());
	}
}
