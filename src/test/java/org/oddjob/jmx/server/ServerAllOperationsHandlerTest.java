package org.oddjob.jmx.server;

import org.junit.Test;

import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.oddjob.OjTestCase;

public class ServerAllOperationsHandlerTest extends OjTestCase {

	interface Fruit {
		
		String getColour();
	}
	
	class MyFruit implements Fruit {
		
		public String getColour() {
			return "pink";
		}
	}
	
   @Test
	public void testInvoke() throws MBeanException, ReflectionException {
	
		ServerAllOperationsHandler<Fruit> test = 
			new ServerAllOperationsHandler<Fruit>(
					Fruit.class, new MyFruit());

		Object result = test.invoke(new MBeanOperation(
				"getColour", new String[0]), new Object[0]);
		
		assertEquals("pink", result);
	}
}
