package org.oddjob.framework;

import junit.framework.TestCase;

public class RunnableProxyGeneratorTest extends TestCase {

	public interface MyInterface {
		
	}
	
	public static class MyJob implements Runnable, MyInterface {
		
		@Override
		public void run() {
		}
	}
	
	public void testAProxyImplementsAllInterfaces() {
		
		MyJob job = new MyJob();
		
		Object proxy = new RunnableProxyGenerator().generate(
				job, getClass().getClassLoader());
		
		assertTrue(proxy instanceof MyInterface);
	}
	
}
