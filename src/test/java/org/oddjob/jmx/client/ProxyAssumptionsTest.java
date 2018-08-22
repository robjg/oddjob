package org.oddjob.jmx.client;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.security.auth.Destroyable;

import org.oddjob.OjTestCase;

public class ProxyAssumptionsTest extends OjTestCase {

	/**
	 * How can we deal with toString()?
	 * - To string is passed through to the invocation handler.
	 */
   @Test
	public void testToString() {
		class H implements InvocationHandler {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				assertEquals(Object.class.getMethod("toString"), method);
				// TODO Auto-generated method stub
				return "Test";
			}
		}
		
		H h = new H();
		
		Object p = Proxy.newProxyInstance(null, new Class[0], h);
		
		String result = p.toString();

		assertEquals("Test", result);
	}
	
	/**
	 * Only methods on the interface or Objec or passed through...
	 *
	 */
   @Test
	public void testAnyMethod() throws Exception {
		class H implements InvocationHandler {
			
			boolean destroyed;
			
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getName().equals("destroy")) {
					destroyed = true;
				}
				else {
					fail("Only expecting destroy");
				}
				return null;
			}
			
			public void foo() {}
		}
		
		H h = new H();
		
		Object p = Proxy.newProxyInstance(
				this.getClass().getClassLoader(), 
				new Class[] { Destroyable.class }, h);
		
		// this works (Proxy is an Object).
		Method m = p.getClass().getMethod("toString");

		// and p is a Destroyable
		m = p.getClass().getMethod("destroy");
		
		// and can even be destoryed by reflection 
		// instead of ((Destoyable) p).destroy();
		m.invoke(p);
		
		assertTrue(h.destroyed);
		
		// but the proxy has to implement the mehthod 
		// not the IH.
		try {
			// this works (Proxy is an Object).
			m = p.getClass().getMethod("foo");
			fail("No such method!");
		} catch (NoSuchMethodException e) {
			// expected
		}
		
		// just to stop eclipse warning!
		h.foo();
	}
}
