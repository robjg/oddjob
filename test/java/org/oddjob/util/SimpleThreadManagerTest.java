package org.oddjob.util;

import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class SimpleThreadManagerTest extends TestCase {
	private static final Logger logger = Logger.getLogger(
			SimpleThreadManagerTest.class);
	
	class OurThing implements Runnable {
		
		Exchanger<Void> exchanger = new Exchanger<Void>();
		
		boolean interrupted;
		
		public void meet() throws InterruptedException {
			exchanger.exchange(null);
		}
		
		@Override
		public void run() {
			try {
				meet();
				meet();
			}
			catch (InterruptedException e) {
				interrupted = true;
				Thread.currentThread().interrupt();
			}
		}	
	}
	
	public void testSimpleRun() throws InterruptedException {
		
		SimpleThreadManager test = new SimpleThreadManager();

		OurThing thing = new OurThing();
		
		test.run(thing, "Our Thing");
		
		String[] descriptions = test.activeDescriptions();
		
		assertEquals(1, descriptions.length);
		
		assertEquals("Our Thing", descriptions[0]);
		
		thing.meet();
		
		thing.meet();
		
		while (test.activeDescriptions().length > 0) {
			logger.info("Waiting for thing to finish.");
			Thread.sleep(1000);
		}
		
		test.close();
		
		assertFalse(thing.interrupted);
	}
	
	public void testStopAll() throws InterruptedException {
		
		SimpleThreadManager test = new SimpleThreadManager();

		OurThing thing = new OurThing();
		
		test.run(thing, "Our Thing");
		
		String[] descriptions = test.activeDescriptions();
		
		assertEquals(1, descriptions.length);
		
		assertEquals("Our Thing", descriptions[0]);
		
		thing.meet();
		
		test.close();
		
		while (test.activeDescriptions().length > 0) {
			logger.info("Waiting for thing to finish.");
			Thread.sleep(1000);
		}
		
		assertTrue(thing.interrupted);
	}
	
	public void testLotsOfThings() throws InterruptedException {
		
		final SimpleThreadManager test = new SimpleThreadManager();

		final AtomicInteger ran = new AtomicInteger();

		for (int i = 0; i < 100; ++i) {
			final int fi = i;
			test.run(new Runnable() {
				@Override
				public void run() {
					test.run(new Runnable() {
						@Override
						public void run() {
							ran.incrementAndGet();
						}
					}, "Second Thing " + fi);
				}
			}, "Thing on 1 " + i);
		}
				
		while (true) {
			String[] descriptions = test.activeDescriptions();
			if (descriptions.length == 0) {
				break;
			}
			for (int i = 0; i < descriptions.length; ++i) {
				logger.info("Waiting for " + descriptions[i]);
			}
			Thread.sleep(1000);
		}
		
		assertEquals(100, ran.intValue());
		
		test.close();
	}
}
