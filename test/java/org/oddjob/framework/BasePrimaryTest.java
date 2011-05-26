package org.oddjob.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

public class BasePrimaryTest extends TestCase {

	private class OurComp extends BasePrimary {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		void work() {
			
			stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
				@Override
				public void run() {
					getStateChanger().setJobState(JobState.EXECUTING);
			
					latch.countDown();

					sleep(0);

					getStateChanger().setJobState(JobState.COMPLETE);
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
	}
	
	public void testSleep() throws InterruptedException {
		
		final OurComp test = new OurComp();
		
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
