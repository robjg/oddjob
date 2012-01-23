package org.oddjob.framework;

import junit.framework.TestCase;

import org.oddjob.Helper;
import org.oddjob.state.JobState;

public class RunnableWrapperStopTest extends TestCase {

	private final class WaitingJob implements Runnable {
		@Override
		public void run() {
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	
	public void testStopViaInterrupt() throws InterruptedException {

		WaitingJob job = new WaitingJob();
		
		Runnable proxy = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) job,
    			getClass().getClassLoader());
    	
		Thread t = new Thread(proxy);
		
		t.start();
		
		t.interrupt();
		
		t.join();
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(proxy));
	}
}
