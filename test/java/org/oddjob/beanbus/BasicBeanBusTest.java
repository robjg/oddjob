package org.oddjob.beanbus;

import java.util.List;

import org.oddjob.beanbus.destinations.BeanCapture;

import junit.framework.TestCase;

public class BasicBeanBusTest extends TestCase {

	private class OurListener implements BusListener {

		int starting;
		int beginning;
		int ending;
		int stopping;
		int crashed;
		int terminated;
		
		@Override
		public void busStarting(BusEvent event) throws BusCrashException {
			++starting;
		}

		@Override
		public void tripBeginning(BusEvent event) throws BusCrashException {
			++beginning;
		}

		@Override
		public void tripEnding(BusEvent event) throws BusCrashException {
			++ending;
		}

		@Override
		public void busStopping(BusEvent event) throws BusCrashException {
			++stopping;
		}

		@Override
		public void busTerminated(BusEvent event) {
			++terminated;
		}

		@Override
		public void busCrashed(BusEvent event, BusException e) {
			++crashed;
		}
		
		@Override
		public void busStopRequested(BusEvent event) {
			// TODO Auto-generated method stub
			
		}
	}
	
	
	public void testStandardLifecycle() throws BusException {
		
		BeanCapture<String> trap = new BeanCapture<String>();
		
		OurListener listener = new OurListener();
		
		BasicBeanBus<String> test = new BasicBeanBus<String>();
		test.setTo(trap);
		test.addBusListener(listener);
		test.startBus();
		
		assertEquals(1, listener.starting);
		assertEquals(0, listener.beginning);
		
		test.accept("apple");
		
		assertEquals(1, listener.beginning);
		
		test.stopBus();

		assertEquals(1, listener.starting);
		assertEquals(1, listener.beginning);
		assertEquals(1, listener.ending);
		assertEquals(1, listener.stopping);
		assertEquals(1, listener.terminated);
		assertEquals(0, listener.crashed);
		
		List<String> results = trap.getBeans();
		
		assertEquals("apple", results.get(0));
	}
	
	private class BadDestination extends AbstractDestination<Object> {
		@Override
		public boolean add(Object bean) {
			throw new IllegalArgumentException();
		}
	}
	
	public void testCrashOnPutLifecycle() throws BusException {
		
		OurListener listener = new OurListener();
		
		BasicBeanBus<String> test = new BasicBeanBus<String>();
		test.setTo(new BadDestination());
		test.addBusListener(listener);
		test.startBus();
		
		assertEquals(1, listener.starting);
		assertEquals(0, listener.beginning);
		
		try {
			test.accept("apple");
			fail("Should fail.");
		}
		catch (BusCrashException e) {
			// expected
		}
		
		assertEquals(1, listener.starting);
		assertEquals(1, listener.beginning);
		assertEquals(0, listener.ending);
		assertEquals(0, listener.stopping);
		
		assertEquals(1, listener.crashed);
		assertEquals(1, listener.terminated);
		
		try {
			test.stopBus();
			fail("Should fail.");
		}
		catch (IllegalStateException e) {
			// expected
		}
		
	}
	
	public void testCleaningLifecycle() throws BusException {
		
		BeanCapture<String> trap = new BeanCapture<String>();
		
		OurListener listener = new OurListener();
		
		BasicBeanBus<String> test = new BasicBeanBus<String>();
		test.setTo(trap);
		test.addBusListener(listener);
		
		// start stop.
		
		test.startBus();
		
		assertEquals(1, listener.starting);
		
		test.stopBus();
		
		assertEquals(1, listener.stopping);
		assertEquals(1, listener.terminated);
		
		// now with clean.
		test.startBus();
		
		assertEquals(2, listener.starting);
		
		test.accept("apple");
		
		assertEquals(1, listener.beginning);
		
		test.cleanBus();
		
		assertEquals(1, listener.ending);
		
		test.accept("pear");
		
		assertEquals(2, listener.beginning);
		
		test.cleanBus();
		
		assertEquals(2, listener.ending);
		
		test.stopBus();
		
		assertEquals(2, listener.starting);
		assertEquals(2, listener.beginning);
		assertEquals(2, listener.ending);
		assertEquals(2, listener.stopping);
		assertEquals(2, listener.terminated);
		
		// no clean before stop
		test.startBus();
		
		test.accept("orange");
		
		test.cleanBus();
		
		test.accept("kiwi");
		
		test.stopBus();
		
		assertEquals(3, listener.starting);
		assertEquals(4, listener.beginning);
		assertEquals(4, listener.ending);
		assertEquals(3, listener.stopping);
		assertEquals(3, listener.terminated);
	}
	
	public void testBusState() throws BusException {
		
		BeanCapture<String> trap = new BeanCapture<String>();
		
		OurListener listener = new OurListener();
		
		BasicBeanBus<String> test = new BasicBeanBus<String>();
		test.setTo(trap);
		test.addBusListener(listener);
		
		// stop before started..
		
		try {
			test.stopBus();
			fail("Should fail!");
		}
		catch (IllegalStateException e) {
			// expected.
		}
		
		assertEquals(0, listener.starting);
		assertEquals(0, listener.beginning);
		assertEquals(0, listener.ending);
		assertEquals(0, listener.stopping);
		assertEquals(0, listener.terminated);
		
		test.startBus();
		
		assertEquals(1, listener.starting);
		
		// start after started.
		try {
			test.startBus();
			fail("Should fail!");
		}
		catch (IllegalStateException e) {
			// expected.
		}
		
		test.stopBus();
		
		assertEquals(1, listener.starting);
		assertEquals(0, listener.beginning);		
		assertEquals(0, listener.ending);
		assertEquals(1, listener.stopping);
		assertEquals(1, listener.terminated);
	}
		
}
