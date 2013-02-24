package org.oddjob.beanbus.destinations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.StateSteps;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.destinations.BeanQueue;
import org.oddjob.state.ParentState;

public class BeanQueueTest extends TestCase {

	private static final Logger logger = Logger.getLogger(BeanQueueTest.class);
	
	public void testQueueStop() throws InterruptedException {
	
		final BeanQueue<String> test = new BeanQueue<String>();
		test.configured();
		
		test.add("apple");
		
		final List<String> results = new ArrayList<String>();
		final CountDownLatch latch = new CountDownLatch(2);
		
		
		Thread t = new Thread() {
			public void run() {
				
				for (String s : test) {
					results.add(s);
					latch.countDown();
				}
			}
		};
		t.start();
		
		test.add("pear");
		
		latch.await();
		
		// Ensure it's blocking
		Thread.sleep(100);
		
		test.stop();
		
		t.join();
		
		assertEquals("apple", results.get(0));
		assertEquals("pear", results.get(1));
		
	}
	
	public void testStopBeforeEmpty() throws InterruptedException {
		
		final BeanQueue<String> test = new BeanQueue<String>();
		test.configured();
		
		test.add("apple");
		test.add("pear");
		
		
		test.stop();
		
		final List<String> results = new ArrayList<String>();
		
		Thread t = new Thread() {
			public void run() {
				
				for (String s : test) {
					results.add(s);
				}
			}
		};
		t.start();
		
		t.join();
		
		assertEquals("apple", results.get(0));
		assertEquals("pear", results.get(1));
		
	}
	
	public void testStartConsumingFirst() throws InterruptedException {
		
		final BeanQueue<String> test = new BeanQueue<String>();
		test.configured();
		
		final List<String> results = new ArrayList<String>();
		
		Thread t = new Thread() {
			public void run() {
				
				for (String s : test) {
					results.add(s);
				}
			}
		};
		t.start();
		
		Thread.sleep(100);
		
		test.add("apple");
		test.add("pear");
				
		test.stop();
		
		t.join();
		
		assertEquals("apple", results.get(0));
		assertEquals("pear", results.get(1));
		
	}
	
	public void testMulitipleConsumers() throws InterruptedException {

		final BeanQueue<Integer> test = new BeanQueue<Integer>();
		test.configured();
		
		class Consumer implements Runnable {
			
			List<Integer> results = new ArrayList<Integer>();
			
			@Override
			public void run() {
				for (Integer i : test) {
					results.add(i);
					Thread.yield();
				}
			}
		}
		
		Consumer consumer1 = new Consumer();
		Consumer consumer2 = new Consumer();
		Consumer consumer3 = new Consumer();
		
		Thread t1 = new Thread(consumer1);
		Thread t2 = new Thread(consumer2);
		Thread t3 = new Thread(consumer3);
		
		t1.start();
		t2.start();
		t3.start();
		
		for (int i = 1; i <= 100000; ++i) {
			test.add(new Integer(i));
		}
		
		Thread.sleep(50);
		
		test.stop();
		
		t1.join();
		t2.join();
		t3.join();
		
		logger.info("c1: " + consumer1.results.size() + 
				", c2: " + consumer2.results.size() + 
				", c3: " + consumer3.results.size());

		assertEquals(100000, consumer1.results.size() + 
				consumer2.results.size() 
				+ consumer3.results.size());
	}	
	
	public void testInOddjob() throws ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/beanbus/destinations/BeanQueueExample.xml", getClass()
						.getClassLoader()));
		
		StateSteps states = new StateSteps(oddjob);
		states.startCheck(ParentState.READY,
				ParentState.EXECUTING, ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		oddjob.run();
		
		states.checkWait();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		List<?> results = lookup.lookup(
				"consumer.to", List.class);
		
		assertEquals("apple", results.get(0));
		assertEquals("orange", results.get(1));
		assertEquals("pear", results.get(2));
		
		
		Object parallel = lookup.lookup("parallel");
		
		((Resetable) parallel).hardReset();
		((Runnable) parallel).run();
		
		results = lookup.lookup(
				"consumer.to", List.class);
		
		assertEquals("apple", results.get(0));
		assertEquals("orange", results.get(1));
		assertEquals("pear", results.get(2));
		
		oddjob.destroy();
	}
}
