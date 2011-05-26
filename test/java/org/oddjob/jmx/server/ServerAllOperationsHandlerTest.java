package org.oddjob.jmx.server;

import javax.management.MBeanException;
import javax.management.ReflectionException;

import junit.framework.TestCase;

public class ServerAllOperationsHandlerTest extends TestCase {

	interface Fruit {
		
		String getColour();
	}
	
	class MyFruit implements Fruit {
		
		public String getColour() {
			return "pink";
		}
	}
	
	public void testInvoke() throws MBeanException, ReflectionException {
	
		ServerAllOperationsHandler<Fruit> test = 
			new ServerAllOperationsHandler<Fruit>(
					Fruit.class, new MyFruit());

		Object result = test.invoke(new MBeanOperation(
				"getColour", new String[0]), new Object[0]);
		
		assertEquals("pink", result);
	}
}
