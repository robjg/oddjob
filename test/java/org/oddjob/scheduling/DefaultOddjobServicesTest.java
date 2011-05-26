package org.oddjob.scheduling;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

public class DefaultOddjobServicesTest extends TestCase {

	class AJob implements Runnable {
		
		int ran;

		public void run() {
			ran++;
		}
	}
	
	public void testRunAndWait() throws InterruptedException, ExecutionException {
		
		DefaultExecutors test = new DefaultExecutors();
			
		ExecutorService poolExecutor = test.getPoolExecutor();
		
		AJob job = new AJob();
		
		Future<?> future = poolExecutor.submit(job);
		
		assertNull(future.get());
		
		assertEquals(1, job.ran);
		
		test.stop();
	}
	
	class SlowJob implements Runnable {
		
		Exchanger<Thread> exchanger = new Exchanger<Thread>();
		
		public void run() {
			try {
				exchanger.exchange(Thread.currentThread());
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public void testInterruptRunningJob() throws InterruptedException, ExecutionException {
		
		DefaultExecutors test = new DefaultExecutors();
		test.setPoolSize(1);
		
		ExecutorService poolExecutor = test.getPoolExecutor();
		
		SlowJob job = new SlowJob();
		
		Future<?> future = poolExecutor.submit(job);
		
		Thread t = job.exchanger.exchange(null);
		
		t.interrupt();
		
		assertNull(future.get());
		
		future = poolExecutor.submit(job);
		
		t = job.exchanger.exchange(null);
		
		assertFalse(t.isInterrupted());
		
		t.interrupt();
		
		test.stop();
	}
	
	class DeadJob implements Runnable {
		
		public void run() {
			try {
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public void testStopServices() throws InterruptedException, ExecutionException {
		
		DefaultExecutors test = new DefaultExecutors();
		test.setPoolSize(1);
		
		ExecutorService poolExecutor = test.getPoolExecutor();
		
		DeadJob job = new DeadJob();
		
		Future<?> future = poolExecutor.submit(job);
		
		test.stop();

		// don't quite understand what's going on here
		// - if shutdown before job starts running get waits
		try {
			assertNull(future.get(500, TimeUnit.MILLISECONDS));
			assertTrue(future.isDone());
			assertFalse(future.isCancelled());
		} catch (TimeoutException e1) {
			assertFalse(future.isDone());
			assertFalse(future.isCancelled());
		}
		
		try {
			future = poolExecutor.submit(job);
			fail("Should throw an exception.");
		}
		catch (RejectedExecutionException e) {
			// expected.
		}
		
	}

}
