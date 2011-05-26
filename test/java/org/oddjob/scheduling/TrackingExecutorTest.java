package org.oddjob.scheduling;

import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class TrackingExecutorTest extends TestCase {
	private static final Logger logger = Logger.getLogger(TrackingExecutorTest.class);

	@Override
	protected void setUp() {
		logger.debug("=============== " + getName() + " ===================");
	}

	class MockRunnable implements Runnable {
		public void run() {
			throw new RuntimeException("Unexpected.");
		}
	}
	
	
	public void testCancel() throws InterruptedException {

		MockRunnable runnable = new MockRunnable();
		
		ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
		
		TrackingExecutor test = new TrackingExecutor(executor);
		
		ScheduledFuture<?> future = test.schedule(
				runnable, 100, TimeUnit.HOURS);
		
		assertEquals(1, test.getTaskCount());
		
		future.cancel(true);

		assertEquals(0, test.getTaskCount());
		
		// And then again.
		future.cancel(true);
		
		assertEquals(0, test.getTaskCount());
		
		// testing no exceptions.
		
		executor.shutdown();
		
		executor.awaitTermination(5, TimeUnit.SECONDS);
		
		assertTrue(executor.isTerminated());
	}
	
	class MyRunable implements Runnable {
		boolean ran;
		public void run() {
			ran = true;
		}
	}
	
	public void testCancelWhenAlreadyRun() throws InterruptedException {

		MyRunable runnable = new MyRunable();
		
		ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
		
		TrackingExecutor test = new TrackingExecutor(executor);
		
		ScheduledFuture<?> future = test.schedule(
				runnable, 100, TimeUnit.MILLISECONDS);
		
		test.waitForNothingOutstanding();
		
		future.cancel(true);

		// And then again.
		future.cancel(true);
		
		// testing no exceptions.
		
		executor.shutdown();
		
		executor.awaitTermination(5, TimeUnit.SECONDS);
		
		assertTrue(executor.isTerminated());
	}
	
	class StubbonRunnable implements Runnable {
		boolean interrupted;
		Exchanger<Void> exchanger = new Exchanger<Void>();
		
		public void run() {
			try {
				exchanger.exchange(null);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			try {
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException e) {
				interrupted = true;
				logger.debug("Interrupted.");
				try {
					exchanger.exchange(null);
				} catch (InterruptedException e2) {
					throw new RuntimeException(e2);
				}
			}
		}
	}
	
	public void testLongRunningCancel() throws InterruptedException {

		StubbonRunnable runnable = new StubbonRunnable();
		
		ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, 
				new RejectedExecutionHandler() {
			public void rejectedExecution(Runnable r,
					ThreadPoolExecutor executor) {
				fail("Unexpected.");
			}
		});
		
		TrackingExecutor test = new TrackingExecutor(executor);
		
		ScheduledFuture<?> future = test.schedule(
				runnable, 0, TimeUnit.MILLISECONDS);

		runnable.exchanger.exchange(null);
		
		assertEquals(1, test.getTaskCount());
		
		future.cancel(true);

		assertEquals(0, test.getTaskCount());

		// interrupt happens on different thread.
		runnable.exchanger.exchange(null);
		
		assertTrue(runnable.interrupted);
		
		List<Runnable> outstanding = test.shutdownNow();
		// guess already running.
		assertEquals(0, outstanding.size());
		
		executor.awaitTermination(5, TimeUnit.SECONDS);
		
		assertTrue(executor.isTerminated());
	}
}
