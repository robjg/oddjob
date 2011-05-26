/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.oddjob.MockStateful;
import org.oddjob.Stateful;

/**
 * 
 */
public class JobStateHandlerTest extends TestCase {

		
	private void setState(final JobStateHandler handler, 
			final JobState state) {
		boolean ran = handler.waitToWhen(new StateCondition() {
			public boolean test(JobState state) {
				return true;
			}
		}, new Runnable() {
			public void run() {
				handler.setJobState(state);
				handler.fireEvent();
			}
		});
		assertTrue(ran);
	}
	
	private void setException(final JobStateHandler handler, 
			final Exception e) {
		boolean ran = handler.waitToWhen(new StateCondition() {
			public boolean test(JobState state) {
				return true;
			}
		}, new Runnable() {
			public void run() {
				handler.setJobStateException(e);
				handler.fireEvent();
			}
		});
		assertTrue(ran);
	}
	
	public void testAllStates() {
		
		JobStateHandler test = new JobStateHandler(new MockStateful());
		
		assertEquals(JobState.READY, test.getJobState());
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
		
		setState(test, JobState.EXECUTING);
		
		assertEquals(JobState.EXECUTING, test.getJobState());
		assertEquals(JobState.EXECUTING, test.lastJobStateEvent().getJobState());

		setState(test, JobState.COMPLETE);
		
		assertEquals(JobState.COMPLETE, test.getJobState());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
		
		setState(test, JobState.INCOMPLETE);
		
		assertEquals(JobState.INCOMPLETE, test.getJobState());
		assertEquals(JobState.INCOMPLETE, test.lastJobStateEvent().getJobState());

		setException(test, new Exception());
		
		assertEquals(JobState.EXCEPTION, test.getJobState());
		assertEquals(JobState.EXCEPTION, test.lastJobStateEvent().getJobState());

		setState( test, JobState.READY);
		
		assertEquals(JobState.READY, test.getJobState());
		assertEquals(JobState.READY, test.lastJobStateEvent().getJobState());
	}

	private class RecordingStateListener implements JobStateListener {
		
		List<JobStateEvent> events = new ArrayList<JobStateEvent>();
		
		public synchronized void jobStateChange(JobStateEvent event) {
			events.add(event);
		}
	}
	
	public void testListenersNotified() {

		Stateful source = new MockStateful();
		JobStateHandler test = new JobStateHandler(source);

		RecordingStateListener l = new RecordingStateListener();
		test.addJobStateListener(l);
		
		assertEquals(1, l.events.size());
		assertEquals(JobState.READY, l.events.get(0).getJobState());
		assertEquals(source, l.events.get(0).getSource());

		setState(test, JobState.EXECUTING);
		
		assertEquals(2, l.events.size());
		assertEquals(JobState.EXECUTING, l.events.get(1).getJobState());

		setState(test, JobState.COMPLETE);
		
		assertEquals(3, l.events.size());
		assertEquals(JobState.COMPLETE, l.events.get(2).getJobState());
		
		setState(test, JobState.INCOMPLETE);
		
		assertEquals(4, l.events.size());
		assertEquals(JobState.INCOMPLETE, l.events.get(3).getJobState());

		Exception e = new Exception();
		setException(test, e);
		
		assertEquals(5, l.events.size());
		assertEquals(JobState.EXCEPTION, l.events.get(4).getJobState());
		assertEquals(e, l.events.get(4).getException());

		setState(test, JobState.READY);
		
		assertEquals(6, l.events.size());
		assertEquals(JobState.READY, l.events.get(5).getJobState());
		
		setState(test, JobState.DESTROYED);
		
		assertEquals(7, l.events.size());
		assertEquals(JobState.DESTROYED, l.events.get(6).getJobState());
	}

	public void testDuplicateEventsNotified() {

		Stateful source = new MockStateful();
		JobStateHandler test = new JobStateHandler(source);

		RecordingStateListener l = new RecordingStateListener();
		test.addJobStateListener(l);
		
		assertEquals(1, l.events.size());
		assertEquals(JobState.READY, l.events.get(0).getJobState());
		assertEquals(source, l.events.get(0).getSource());

		setState(test, JobState.READY);
		
		assertEquals(2, l.events.size());
		
		setState(test, JobState.EXECUTING);
		
		assertEquals(3, l.events.size());
		
		setState(test, JobState.EXECUTING);
		
		assertEquals(4, l.events.size());
		assertEquals(JobState.EXECUTING, l.events.get(3).getJobState());

		setState(test, JobState.COMPLETE);
		
		assertEquals(5, l.events.size());
		assertEquals(JobState.COMPLETE, l.events.get(4).getJobState());
	}
	
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
		
		test.addJobStateListener(l1);
		test.addJobStateListener(l2);
		test.addJobStateListener(l3);
		
		t.start();
		
		t.join();
		
		assertEquals(2, l1.events.size());
		assertEquals(2, l2.events.size());
		assertEquals(2, l3.events.size());
		
		assertEquals(JobState.COMPLETE, l1.events.get(1).getJobState());
		assertEquals(JobState.COMPLETE, l2.events.get(1).getJobState());
		assertEquals(JobState.COMPLETE, l3.events.get(1).getJobState());
	}
	
	
	public void testListenerConcurrentModification() {
		
		final JobStateHandler test = new JobStateHandler(new MockStateful());

		test.addJobStateListener(new JobStateListener() {
			public void jobStateChange(JobStateEvent event) {
				if (event.getJobState() == JobState.COMPLETE) {
					test.removeJobStateListener(this);
				}
			}
		});
		
		assertEquals(1, test.listenerCount());
		
		test.waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				test.setJobState(JobState.COMPLETE);
				test.fireEvent();
			}
		});
		
		assertEquals(0, test.listenerCount());
	}
	
	public void testThatAttemptsToChangeState() {
		
		final JobStateHandler test = new JobStateHandler(new MockStateful());
		
		JobStateListener listener = new JobStateListener() {
			
			@Override
			public void jobStateChange(JobStateEvent event) {
				test.waitToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						test.setJobState(JobState.COMPLETE);
						test.fireEvent();
					}
				});
			}
		};
		
		try {
			test.addJobStateListener(listener);
			fail("Expected to fail.");
		}
		catch (IllegalStateException e) {
			// expected
		}

		final AtomicBoolean failed = new AtomicBoolean();
		
		JobStateListener listener2 = new JobStateListener() {
			
			@Override
			public void jobStateChange(final JobStateEvent event) {
				
				if (JobState.INCOMPLETE == event.getJobState()) {
					try {
						test.setJobState(JobState.COMPLETE);
						test.fireEvent();
					}
					catch (IllegalStateException e) {
						failed.set(true);
					}
				}
			}
		};

		test.addJobStateListener(listener2);
		
		test.waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				test.setJobState(JobState.INCOMPLETE);
				test.fireEvent();
			}
		});
		
		assertTrue(failed.get());
		assertEquals(JobState.INCOMPLETE, test.lastJobStateEvent().getJobState());
	}
	
	public void testListenerNotificationOrder() throws InterruptedException {
		
		final JobStateHandler test = new JobStateHandler(new MockStateful());
		
		final Exchanger<Void> exchanger = new Exchanger<Void>();
		
		test.addJobStateListener(new JobStateListener() {
			
			@Override
			public void jobStateChange(JobStateEvent event) {
				try {
					if (event.getJobState() == JobState.COMPLETE) {
						exchanger.exchange(null);
						exchanger.exchange(null);
					}
				} catch (InterruptedException e) {
					throw new RuntimeException("Unexpected", e);
				}
			}
		});
		
		final List<JobState> events = new ArrayList<JobState>();
		
		JobStateListener listener = new JobStateListener() {
			
			@Override
			public void jobStateChange(JobStateEvent event) {
				events.add(event.getJobState());
			}
		};
		
		new Thread() {
			@Override
			public void run() {
				test.waitToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						test.setJobState(JobState.COMPLETE);
						test.fireEvent();
					}
				});
			}
		}.start();

		exchanger.exchange(null);
		exchanger.exchange(null);
				
		// able to get event before other listener complete.
		test.addJobStateListener(listener);
		
		assertEquals(JobState.COMPLETE, events.get(0));
		assertEquals(1, events.size());
		assertEquals(JobState.COMPLETE, test.lastJobStateEvent().getJobState());
	}
	
	public void testSleep() throws InterruptedException {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		final JobStateHandler test = new JobStateHandler(new MockStateful());
		
		final List<JobState> events = new ArrayList<JobState>();
		
		JobStateListener listener = new JobStateListener() {
			
			@Override
			public void jobStateChange(JobStateEvent event) {
				events.add(event.getJobState());
			}
		};
		
		test.addJobStateListener(listener);
		
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
						test.setJobState(JobState.COMPLETE);
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
						test.setJobState(JobState.INCOMPLETE);
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
