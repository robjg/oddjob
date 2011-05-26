package org.oddjob.scheduling;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

public class JavaExecutorAssumptionsTest extends TestCase {

	class SimpleRunnable implements Runnable {
		boolean ran;
		
		public void run() {
			ran = true;
		}
	}
	
	public void testCancelBeforeRun() throws InterruptedException {
		
		ScheduledExecutorService executor = 
			new ScheduledThreadPoolExecutor(1);

		SimpleRunnable runnable = new SimpleRunnable();
		
		ScheduledFuture<?> future = executor.schedule(
				runnable, 1000, TimeUnit.HOURS);

		Thread.sleep(1);
		
		assertTrue(future.getDelay(TimeUnit.MILLISECONDS) < (1000L * 60 * 60 * 1000));
		
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		
		future.cancel(true);
		
		assertEquals(true, future.isCancelled());
		assertEquals(true, future.isDone());
		
		assertEquals(false, runnable.ran);
		
		executor.shutdown();
	}

	public void testRunBeforeCancel() 
	throws InterruptedException, ExecutionException {
		
		ScheduledExecutorService executor = 
			new ScheduledThreadPoolExecutor(1);

		SimpleRunnable runnable = new SimpleRunnable();
		
		Future<?> future = executor.schedule(
				runnable, 0, TimeUnit.HOURS);

		future.get();
		
		assertEquals(true, runnable.ran);
		
		assertEquals(false, future.isCancelled());
		assertEquals(true, future.isDone());
		
		future.cancel(true);
		
		assertEquals(false, future.isCancelled());
		
		executor.shutdown();
	}

	class RunUntilInerrupted implements Runnable {
	
		Exchanger<Void> exchange = new Exchanger<Void>();
		boolean ran;
		boolean interrupted;
		
		public synchronized void run() {
			ran = true;
			try {
				exchange.exchange(null);
				wait();
			} catch (InterruptedException e) {
				interrupted = true;
			}
			try {
				exchange.exchange(null);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void testCancelInterrupted() 
	throws InterruptedException, ExecutionException {
		
		ScheduledExecutorService executor = 
			new ScheduledThreadPoolExecutor(1);

		RunUntilInerrupted runnable = new RunUntilInerrupted();
		
		Future<?> future = executor.schedule(
				runnable, 0, TimeUnit.HOURS);
		
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());

		runnable.exchange.exchange(null);
		
		future.cancel(true);

		try {
			future.get();
			fail("Should cancel.");
		}
		catch (CancellationException e) {
			// expected.
		}

		// CancellationException thrown even though the job hasn't actually finished.
		// need this exchange to wait for the finish.
		runnable.exchange.exchange(null);
		
		assertEquals(true, runnable.ran);
		assertEquals(true, runnable.interrupted);
		
		assertEquals(true, future.isCancelled());
		assertEquals(true, future.isDone());
		
		executor.shutdown();
	}
		
}
