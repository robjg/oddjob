package org.oddjob.framework;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.IconSteps;
import org.oddjob.Iconic;
import org.oddjob.OddjobTestHelper;
import org.oddjob.Stoppable;
import org.oddjob.images.IconHelper;
import org.oddjob.state.JobState;

public class RunnableWrapperStopTest extends TestCase {

	private final class WaitingJob implements Runnable {
		
		CyclicBarrier barrier = new CyclicBarrier(2);
		
		@Override
		public void run() {
			try {
				barrier.await();
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1);
			} catch (BrokenBarrierException e1) {
				throw new RuntimeException(e1);
			}
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	
	public void testStopViaInterrupt() throws InterruptedException, BrokenBarrierException, FailedToStopException {

		WaitingJob job = new WaitingJob();
		
		Runnable proxy = (Runnable) new RunnableProxyGenerator().generate(
    			job,
    			getClass().getClassLoader());
    	
		IconSteps icons = new IconSteps((Iconic) proxy);
		icons.startCheck(IconHelper.READY, IconHelper.EXECUTING, 
				IconHelper.STOPPING, IconHelper.COMPLETE);
		
		Thread t = new Thread(proxy);

		t.start();
		
		job.barrier.await();
		
		((Stoppable) proxy).stop();
		
		t.join();
		
		icons.checkNow();
		
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(proxy));
	}
}
