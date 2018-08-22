package org.oddjob.framework.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.scheduling.MockExecutorService;
import org.oddjob.scheduling.MockFuture;

public class AsyncExecutionSupportTest extends OjTestCase {

	private class OurExecutor extends MockExecutorService {
		
		List<Runnable> submitted = new ArrayList<Runnable>();
		
		@Override
		public Future<?> submit(Runnable task) {
			submitted.add(task);
			return new MockFuture<Object>() {
			};
		}
	}
	
   @Test
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
	
   @Test
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
