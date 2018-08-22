package org.oddjob.framework.adapt.job;

import java.util.concurrent.Callable;

import org.junit.Test;
import org.oddjob.OjTestCase;

public class CallableProxyGeneratorTest extends OjTestCase {

	public interface MyInterface {
		
	}
	
	public static class MyJob implements Callable<Integer>, MyInterface {
		@Override
		public Integer call() throws Exception {
			return 0;
		}
	}
	
   @Test
	public void testAProxyImplementsAllInterfaces() {
		
		MyJob callable = new MyJob();
		
		Object proxy = new CallableProxyGenerator().generate(
				callable, getClass().getClassLoader());
		
		assertTrue(proxy instanceof MyInterface);
	}
	
}
