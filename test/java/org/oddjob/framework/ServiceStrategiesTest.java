package org.oddjob.framework;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;

public class ServiceStrategiesTest extends TestCase {

	ArooaSession session = new StandardArooaSession();
	
	public static class MyService1 implements Service {
		boolean started;
		@Override
		public void start() throws Exception {
			started = true;
		}
		@Override
		public void stop() throws FailedToStopException {
			started = false;
		}
	}
	
	public void testIsServiceAlreadyStrategy() throws Exception {
		
		ServiceStrategy test = 
				new ServiceStrategies().isServiceAlreadyStrategy();
		
		MyService1 service = new MyService1();
		
		ServiceAdaptor adaptor = test.serviceFor(service, session);
		
		assertNotNull(adaptor);
		
		assertEquals(service, adaptor.getComponent());

		adaptor.start();
		
		assertEquals(true, service.started);
		
		adaptor.stop();
		
		assertEquals(false, service.started);
		
		assertNull(test.serviceFor(new Object(), session));
	}
	
	public static class MyService2 {
		boolean started;
		@Start
		public void begin() { 
			started = true;
		}
		@Stop
		public void end() {
			started = false;
		}
	}
	
	public void testHasServiceAnotationsStrategy() throws Exception {
		
		ServiceStrategy test = 
				new ServiceStrategies().hasServiceAnnotationsStrategy();
		
		MyService2 service = new MyService2();
		
		ServiceAdaptor adaptor = test.serviceFor(service, session);
		
		assertNotNull(adaptor);
		
		assertEquals(service, adaptor.getComponent());

		adaptor.start();
		
		assertEquals(true, service.started);
		
		adaptor.stop();
		
		assertEquals(false, service.started);
		
		assertNull(test.serviceFor(new Object(), session));
	}
	
	public static class MyService3 {
		boolean started;

		public void start() { 
			started = true;
		}
		
		public void stop() {
			started = false;
		}
	}

	public void testHasServiceMethodsStrategy() throws Exception {
		
		ServiceStrategy test = 
				new ServiceStrategies().hasServiceMethodsStrategy();
		
		MyService3 service = new MyService3();
		
		ServiceAdaptor adaptor = test.serviceFor(service, session);
		
		assertNotNull(adaptor);
		
		assertEquals(service, adaptor.getComponent());

		adaptor.start();
		
		assertEquals(true, service.started);
		
		adaptor.stop();
		
		assertEquals(false, service.started);
		
		assertNull(test.serviceFor(new Object(), session));
	}
}
