package org.oddjob.framework;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.persist.Persistable;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateChanger;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.StateListener;
import org.oddjob.state.State;
import org.oddjob.state.StateChanger;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateHandler;

public class BasePrimaryTest extends OjTestCase {

	private class OurComp extends BasePrimary {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		private final JobStateHandler stateHandler = new JobStateHandler(this);
		
		IconHelper iconHelper = new IconHelper(this, 
				StateIcons.iconFor(stateHandler.getState()));
		
		private final JobStateChanger stateChanger;
		
		protected OurComp() {
			stateChanger = new JobStateChanger(stateHandler, iconHelper, 
					new Persistable() {					
						@Override
						public void persist() throws ComponentPersistException {
							save();
						}
					});
		}

		@Override
		protected StateHandler<?> stateHandler() {
			return stateHandler;
		}
		
		@Override
		protected IconHelper iconHelper() {
			return iconHelper;
		}
		
		protected StateChanger<JobState> getStateChanger() {
			return stateChanger;
		}
		
		synchronized void work() {
			
			stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
				@Override
				public void run() {
					getStateChanger().setState(JobState.EXECUTING);
			
					latch.countDown();

					try {
						stateHandler.sleep(0);
					} catch (InterruptedException e) {
						throw new RuntimeException("Unexpected");
					}

					getStateChanger().setState(JobState.COMPLETE);
				}
			});
			
		}
		
		void wakeUp() {
			
			try {
				latch.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				@Override
				public void run() {
					stateHandler.wake();
				}
			});
		}
		
		@Override
		protected void fireDestroyedState() {
			throw new RuntimeException("Unexpected");
		}
	}
	
   @Test
	public void testSleep() throws InterruptedException {
		
		final OurComp test = new OurComp();
		
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
				test.work();
			}
		});
		
		Thread t2 = new Thread() {
			@Override
			public void run() {
				test.wakeUp();
			}
		};

		t1.start();
		t2.start();
		
		t1.join();
		t2.join();
		
		assertEquals(JobState.READY, events.get(0));
		assertEquals(JobState.EXECUTING, events.get(1));
		assertEquals(JobState.COMPLETE, events.get(2));
		
		assertEquals(3, events.size());
	}
}
