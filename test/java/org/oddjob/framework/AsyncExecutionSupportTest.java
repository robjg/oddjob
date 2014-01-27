package org.oddjob.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.oddjob.scheduling.MockExecutorService;
import org.oddjob.scheduling.MockFuture;

public class AsyncExecutionSupportTest extends TestCase {

	private class OurExecutor extends MockExecutorService {
		
		List<Runnable> submitted = new ArrayList<Runnable>();
		
		@Override
		public Future<?> submit(Runnable task) {
			submitted.add(task);
			return new MockFuture<Object>() {
			};
		}
	}
	
	public void testAddTwoJobs() {
		
		OurExecutor executor = new OurExecutor();
		
		final AtomicBoolean done = new AtomicBoolean();
		
		AsyncExecutionSupport test = new AsyncExecutionSupport(new Runnable() {
			
			@Override
			public void run() {
				done.set(true);
			}
		});
		
		test.submitJob(executor, new Runnable() {
			@Override
			public void run() {
			}
		});
		
		test.submitJob(executor, new Runnable() {
			@Override
			public void run() {
			}
		});
		
		test.startWatchingJobs();
		
		assertEquals(false, done.get());
		
		executor.submitted.get(1).run();
		
		assertEquals(false, done.get());
		
		executor.submitted.get(0).run();
		
		assertEquals(true, done.get());
	}
	
	public void testStopBeforeAnyJobsSubmitted() {
		
		final AtomicBoolean done = new AtomicBoolean();
		
		AsyncExecutionSupport test = new AsyncExecutionSupport(new Runnable() {
			
			@Override
			public void run() {
				done.set(true);
			}
		});
	
		test.startWatchingJobs();
		
		assertEquals("done=" + done.get(), true, done.get());
	}
}
