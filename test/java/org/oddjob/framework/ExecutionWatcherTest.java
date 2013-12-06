package org.oddjob.framework;

import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

public class ExecutionWatcherTest extends TestCase {

	public void testAddTwoJobs() {
	
		final AtomicBoolean done = new AtomicBoolean();
		
		ExecutionWatcher test = new ExecutionWatcher(new Runnable() {
			
			@Override
			public void run() {
				done.set(true);
			}
		});
		
		Runnable job1 = test.addJob(new Runnable() {
			@Override
			public void run() {
			}
		});
		
		Runnable job2 = test.addJob(new Runnable() {
			@Override
			public void run() {
			}
		});
		
		test.start();
		
		assertEquals(false, done.get());
		
		job2.run();
		
		assertEquals(false, done.get());
		
		job1.run();
		
		assertEquals(true, done.get());
	}
	
	public void testStopBeforeJobRan() {
		
		final AtomicBoolean done = new AtomicBoolean();
		
		ExecutionWatcher test = new ExecutionWatcher(new Runnable() {
			
			@Override
			public void run() {
				done.set(true);
			}
		});
		
		test.addJob(new Runnable() {
			@Override
			public void run() {
			}
		});
		
		Runnable job2 = test.addJob(new Runnable() {
			@Override
			public void run() {
			}
		});
		
		test.start();
		
		assertEquals(false, done.get());
		
		job2.run();
		
		test.stop();
		
		assertEquals(true, done.get());
	}
	
	public void testStopBeforeStart() {
		
		final AtomicBoolean done = new AtomicBoolean();
		
		ExecutionWatcher test = new ExecutionWatcher(new Runnable() {
			
			@Override
			public void run() {
				done.set(true);
			}
		});
		
		test.addJob(new Runnable() {
			@Override
			public void run() {
			}
		});
		
		test.addJob(new Runnable() {
			@Override
			public void run() {
			}
		});
		
		
		test.stop();
		
		assertEquals(false, done.get());
		
		test.start();
		
		assertEquals(true, done.get());
	}
}
