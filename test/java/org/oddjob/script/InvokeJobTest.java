package org.oddjob.script;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stoppable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.persist.MapPersister;
import org.oddjob.persist.OddjobPersister;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

public class InvokeJobTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(InvokeJobTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("--------------------  " + getName() + "  -------------------------");
	}
	
	public void testMethodExample() throws ArooaPropertyException, ArooaConversionException {
		
		OddjobPersister persister = new MapPersister();
		
		Oddjob oddjob1 = new Oddjob();
		oddjob1.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/InvokeJobMethod.xml", 
				getClass().getClassLoader()));
		oddjob1.setPersister(persister);
		
		oddjob1.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob1.lastStateEvent().getState());
		
		OddjobLookup lookup1 = new OddjobLookup(oddjob1);
				
		String result1 = lookup1.lookup("echo.text", String.class);
		
		assertEquals("Hello", result1);
		
		oddjob1.destroy();
		
		Oddjob oddjob2 = new Oddjob();
		oddjob2.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/InvokeJobMethod.xml", 
				getClass().getClassLoader()));
		oddjob2.setPersister(persister);
		
		oddjob2.load();
		
		assertEquals(ParentState.READY, 
				oddjob2.lastStateEvent().getState());
		
		OddjobLookup lookup2 = new OddjobLookup(oddjob2);
				
		String result2 = lookup2.lookup("invoke-job.result", String.class);
		
		assertEquals("Hello", result2);
		
		oddjob2.destroy();
	}
	
	
	public static class Foo {
		CountDownLatch inMethod = new CountDownLatch(1);
		
		public void takeALongTime() throws InterruptedException {
			inMethod.countDown();
			Thread.sleep(1000000);
		}
	}
	
	public void testStop() throws InterruptedException, FailedToStopException {
		
		Foo foo = new Foo();
		
		InvokeJob test = new InvokeJob();
		test.setFunction("takeALongTime");
		test.setSource(new MethodInvoker(foo));
		test.setArooaSession(new StandardArooaSession());
		
		assertTrue(test instanceof Stoppable);
		
		Thread t = new Thread(test);
		t.start();
		
		assertTrue(foo.inMethod.await(5, TimeUnit.SECONDS));
		
		test.stop();	
	}
	
	public void testStaticMethodExample() throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/InvokeJobStatic.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture capture = new ConsoleCapture();
		capture.capture(Oddjob.CONSOLE);
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		capture.close();
		
		assertEquals("Calculating price for Red Apples", 
				capture.getLines()[0].trim());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
				
		Double result = lookup.lookup("invoke-totalPrice.result", Double.class);
		
		assertEquals(1135.20, (double) result, 0.001);
		
		oddjob.destroy();		
	}
	
	public static double totalPrice(String fruit, int quantity, double price) {
		System.out.println("Calculating price for " + fruit);
		return quantity * price;
	}
}
