package org.oddjob.framework;

import org.junit.Test;

import java.beans.ExceptionListener;
import java.lang.reflect.Method;

import org.oddjob.OjTestCase;

public class ServiceMethodAdaptorTest extends OjTestCase {

	public static class MyService {
		
		boolean started;
		
		boolean stopped;
		
		ExceptionListener exceptionListener;
		
		public void myStart() {
			started = true;
		}
		
		public void myStop() {
			stopped = true;
		}
		
		public void giveMeExceptionListener(ExceptionListener listener) {
			this.exceptionListener = listener;
		}
	}
	
	
   @Test
	public void testAllMethods() throws Exception {
		
		MyService service = new MyService();
		
		Method startMethod = service.getClass().getMethod("myStart");
		Method stopMethod = service.getClass().getMethod("myStop");
		Method exceptionListenerMethod = service.getClass(
				).getMethod("giveMeExceptionListener", ExceptionListener.class);
		
		ServiceMethodAdaptor test = new ServiceMethodAdaptor(
				service, startMethod, stopMethod, exceptionListenerMethod);
		
		test.start();
		
		assertEquals(true, service.started);
		
		test.stop();
		
		assertEquals(true, service.started);
		
		ExceptionListener el = new ExceptionListener() {
			@Override
			public void exceptionThrown(Exception e) {
			}
		};
		
		test.acceptExceptionListener(el);
		
		assertSame(el, service.exceptionListener);
		
	}
}
