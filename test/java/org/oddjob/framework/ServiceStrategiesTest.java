package org.oddjob.framework;

import java.beans.ExceptionListener;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;

public class ServiceStrategiesTest extends TestCase {

	ArooaSession session = new StandardArooaSession();
	
	public static class MyService1 implements Service, FallibleComponent {
		boolean started;
		ExceptionListener el;
		@Override
		public void start() throws Exception {
			started = true;
		}
		@Override
		public void stop() throws FailedToStopException {
			started = false;
		}
		
		@Override
		public void acceptExceptionListener(ExceptionListener exceptionListener) {
			this.el = exceptionListener;
		}
	}
	
	public void testIsServiceAlreadyStrategy() throws Exception {
		
		ServiceStrategy test = 
				new ServiceStrategies.IsServiceAlreadyStrategy();
		
		MyService1 service = new MyService1();
		
		ServiceAdaptor adaptor = test.serviceFor(service, session);
		
		assertNotNull(adaptor);
		
		assertEquals(service, adaptor.getComponent());

		adaptor.start();
		
		assertEquals(true, service.started);
		
		adaptor.stop();
		
		assertEquals(false, service.started);
		
		ExceptionListener el = new ExceptionListener() {
			@Override
			public void exceptionThrown(Exception e) {
			}
		};
		
		adaptor.acceptExceptionListener(el);
				
		assertNull(test.serviceFor(new Object(), session));
	}
	
	public static class MyService2 {
		boolean started;
		ExceptionListener el;
		
		@Start
		public void begin() { 
			started = true;
		}
		@Stop
		public void end() {
			started = false;
		}
		
		@AcceptExceptionListener
		public void el(ExceptionListener el) {
			this.el = el;
		}
	}
	
	public void testHasServiceAnotationsStrategy() throws Exception {
		
		ServiceStrategy test = 
				new ServiceStrategies.HasServiceAnnotationsStrategy();
		
		MyService2 service = new MyService2();
		
		ServiceAdaptor adaptor = test.serviceFor(service, session);
		
		assertNotNull(adaptor);
		
		assertEquals(service, adaptor.getComponent());

		adaptor.start();
		
		assertEquals(true, service.started);
		
		adaptor.stop();
		
		assertEquals(false, service.started);
		
		ExceptionListener el = new ExceptionListener() {
			@Override
			public void exceptionThrown(Exception e) {
			}
		};
		
		adaptor.acceptExceptionListener(el);
		
		assertSame(el, service.el);
		
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
				new ServiceStrategies.HasServiceMethodsStrategy();
		
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
