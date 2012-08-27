package org.oddjob.framework;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import junit.framework.TestCase;

import org.oddjob.Helper;
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
	
	public void testStopViaInterrupt() throws InterruptedException, BrokenBarrierException {

		WaitingJob job = new WaitingJob();
		
		Runnable proxy = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) job,
    			getClass().getClassLoader());
    	
		Thread t = new Thread(proxy);

		t.start();
		
		job.barrier.await();
		
		t.interrupt();
		
		t.join();
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(proxy));
	}
}
