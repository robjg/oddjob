package org.oddjob.framework;

import java.util.concurrent.Callable;

import junit.framework.TestCase;

public class CallableProxyGeneratorTest extends TestCase {

	public interface MyInterface {
		
	}
	
	public static class MyJob implements Callable<Integer>, MyInterface {
		@Override
		public Integer call() throws Exception {
			return 0;
		}
	}
	
	public void testAProxyImplementsAllInterfaces() {
		
		MyJob callable = new MyJob();
		
		Object proxy = new CallableProxyGenerator().generate(
				callable, getClass().getClassLoader());
		
		assertTrue(proxy instanceof MyInterface);
	}
	
}
