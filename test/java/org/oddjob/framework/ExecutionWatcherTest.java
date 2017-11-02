package org.oddjob.framework;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.oddjob.OjTestCase;

public class ExecutionWatcherTest extends OjTestCase {

   @Test
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
	
   @Test
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
	
   @Test
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
	
	// How Cascade would like to use it.
   @Test
	public void testAddJobAfterStart() {
		
		final AtomicBoolean done = new AtomicBoolean();
		
		final ExecutionWatcher test = new ExecutionWatcher(new Runnable() {
			
			@Override
			public void run() {
				done.set(true);
			}
		});

		final AtomicReference<Runnable> job2 = 
				new AtomicReference<Runnable>();
		
		Runnable job1 = test.addJob(new Runnable() {
			@Override
			public void run() {
				job2.set(test.addJob(new Runnable() {
					@Override
					public void run() {
					}
				}));
			}
		});
		
		test.start();
		
		job1.run();
		
		assertEquals(false, done.get());
		
		job2.get().run();
		
		assertEquals(true, done.get());
	}
}
