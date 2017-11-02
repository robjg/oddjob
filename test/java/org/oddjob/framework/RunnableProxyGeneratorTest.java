package org.oddjob.framework;

import org.junit.Test;

import org.oddjob.OjTestCase;

public class RunnableProxyGeneratorTest extends OjTestCase {

	public interface MyInterface {
		
	}
	
	public static class MyJob implements Runnable, MyInterface {
		
		@Override
		public void run() {
		}
	}
	
   @Test
	public void testAProxyImplementsAllInterfaces() {
		
		MyJob job = new MyJob();
		
		Object proxy = new RunnableProxyGenerator().generate(
				job, getClass().getClassLoader());
		
		assertTrue(proxy instanceof MyInterface);
	}
	
}
