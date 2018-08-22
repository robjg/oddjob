package org.oddjob.beanbus.destinations;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.BasicBeanBus;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

public class BeanQueueTest extends Assert {

	private static final Logger logger = LoggerFactory.getLogger(BeanQueueTest.class);
	
	@Rule public TestName name = new TestName();

	public String getName() {
        return name.getMethodName();
    }

   @Before
   public void setUp() throws Exception {

		
		logger.info("--------------------------  " + getName() + 
				"  ------------------------");
	}
	
    @Test
	public void testQueueStop() throws InterruptedException, BusCrashException {

		final BeanQueue<String> test = new BeanQueue<String>();
		test.init();
		
		BasicBeanBus<String> bus = new BasicBeanBus<String>();
		bus.setTo(test);
		test.setBeanBus(bus.getBusConductor());
		
		bus.startBus();
		
		bus.add("apple");
		
		final List<String> results = new ArrayList<String>();
		final CountDownLatch latch = new CountDownLatch(1);
				
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
		
		bus.stopBus();
		
		t.join();
		
		assertEquals("apple", results.get(0));
		assertEquals("pear", results.get(1));
		
	}
	
    @Test
	public void testStopBeforeEmpty() throws InterruptedException {
		
		final BeanQueue<String> test = new BeanQueue<String>();
		test.init();
		
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
	
    @Test
	public void testStartConsumingFirst() throws InterruptedException {
		
		final BeanQueue<String> test = new BeanQueue<String>();
		test.init();
		
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
	
    @Test
	public void testMulitipleConsumers() throws InterruptedException {

		final BeanQueue<Integer> test = new BeanQueue<Integer>();
		test.init();
		
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
	
    @Test
	public void testInOddjob() throws ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/beanbus/destinations/BeanQueueExample.xml", getClass()
						.getClassLoader()));
		
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Stateful parallel = lookup.lookup("parallel", Stateful.class);
		
		StateSteps parallelStates = new StateSteps(parallel);
		parallelStates.startCheck(ParentState.READY,
				ParentState.EXECUTING, ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		oddjob.run();
		
		parallelStates.checkWait();
		
		List<?> results = lookup.lookup(
				"consumer.to", List.class);
		
		logger.info("** Got " + results.size() + " results.");
		
		assertEquals("apple", results.get(0));
		assertEquals("orange", results.get(1));
		assertEquals("pear", results.get(2));
				
		// We must guarantee producer runs first because it must
		// clear the queue.
		
		Object producer = lookup.lookup("producer");
		
		logger.info("** Re-run producer.");
		
		((Resetable) producer).hardReset();
		((Runnable) producer).run();
		
		Object consumer = lookup.lookup("consumer");
		
		logger.info("** Re-run consumer.");
		
		((Resetable) consumer).hardReset();
		((Runnable) consumer).run();
		
		
		results = lookup.lookup(
				"consumer.to", List.class);
		
		logger.info("** Got " + results.size() + " results.");
		
		assertEquals("apple", results.get(0));
		assertEquals("orange", results.get(1));
		assertEquals("pear", results.get(2));
		
		oddjob.destroy();
	}
	
    @Test
	public void testBeanBusExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/beanbus/destinations/BeanQueueExample2.xml", getClass()
						.getClassLoader()));
		
		StateSteps states = new StateSteps(oddjob);
		states.startCheck(ParentState.READY,
				ParentState.EXECUTING,
				ParentState.COMPLETE);
		
		logger.info("** First Run.");
		
		oddjob.run();
		
		states.checkNow();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		List<?> results = lookup.lookup(
				"results.beans", List.class);
		
		assertEquals("Apple", results.get(0));
		assertEquals("Orange", results.get(1));
		assertEquals("Banana", results.get(2));
		assertEquals("Pear", results.get(3));
		assertEquals("Kiwi", results.get(4));
		
		
		Object parallel = lookup.lookup("parallel");
		
		logger.info("** Reset.");
		
		((Resetable) parallel).hardReset();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		states.startCheck(ParentState.READY,
				ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		logger.info("** Second Run.");
		
		((Runnable) parallel).run();
		
		states.checkNow();
		
		logger.info("** Complete.");
		
		results = lookup.lookup(
				"results.beans", List.class);
		
		
		assertEquals("Apple", results.get(0));
		assertEquals("Orange", results.get(1));
		assertEquals("Banana", results.get(2));
		assertEquals("Pear", results.get(3));
		assertEquals("Kiwi", results.get(4));
		
		oddjob.destroy();
	}
}
