package org.oddjob.framework;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;

public class ServiceProxyGeneratorTest extends TestCase {

	public interface MyInterface {
		
	}
	
	public static class MyService implements MyInterface {
		
		@Start
		public void start() throws Exception {
		}
		
		@Stop
		public void stop() throws FailedToStopException {
		}
	}
	
	public void testAProxyImplementsAllInterfaces() {
		
		ArooaSession session = new StandardArooaSession();
		
		MyService service = new MyService();
		
		ServiceAdaptor adaptor = 
				new ServiceStrategies().serviceFor(service, session);
		
		Object proxy = new ServiceProxyGenerator().generate(
				adaptor, getClass().getClassLoader());
		
		assertTrue(proxy instanceof MyInterface);
		assertFalse(proxy instanceof Service);
	}
	
}
