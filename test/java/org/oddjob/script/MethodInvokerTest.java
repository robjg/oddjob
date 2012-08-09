package org.oddjob.script;

import java.util.Date;

import junit.framework.TestCase;

import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.utils.DateHelper;

public class MethodInvokerTest extends TestCase {

	public static String echo(String value) {
		return value;
	}
	
	public class Simple {
		
		Date delivery;
		int quantity;
		
		public double quote(Date delivery, int quantity) {
			
			this.delivery= delivery;
			this.quantity = quantity;
			
			return 94.23;
		}
	}
	
	public void testInvoking() throws Exception {
		
		Simple simple = new Simple();
		
		MethodInvoker test = new MethodInvoker(simple);

		InvokerArguments args = new ConvertableArguments(
				new DefaultConverter(), "2012-08-01", 42);
		
		double result = (Double) test.invoke("quote", args);
		
		assertEquals(94.23, result, 0.001);
		assertEquals(DateHelper.parseDate("2012-08-01"), simple.delivery);
		assertEquals(42, simple.quantity);
		
	}
	
	public void testStatic() throws Exception {
		
		MethodInvoker test = new MethodInvoker(MethodInvokerTest.class);

		InvokerArguments args = new ConvertableArguments(
				new DefaultConverter(), "Hello");
		
		String result = (String) test.invoke("static echo", args);
		
		assertEquals("Hello", result);
	}
}
