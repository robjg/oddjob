package org.oddjob.jmx.client;

import org.junit.Test;

import org.oddjob.OjTestCase;

public class ClientInterfaceManagerFactoryTest extends OjTestCase {

	interface Foo {
		public void foo();
	}
	
   @Test
	public void testInvoke() throws Throwable {
		
		class MockFoo implements Foo {
			boolean invoked;
			
			public void foo() {
				invoked = true;
			}
		}
		MockFoo foo = new MockFoo();
		
		class FooClientHandlerFactory extends MockClientInterfaceHandlerFactory<Foo> {
			
			public Foo createClientHandler(Foo proxy, ClientSideToolkit toolkit) {
				return proxy;
			};
			
			public Class<Foo> interfaceClass() {
				return Foo.class;
			};
		}		
		
		ClientInterfaceManagerFactory test = new ClientInterfaceManagerFactory (
				new ClientInterfaceHandlerFactory[] { 
						new FooClientHandlerFactory()
				});
		
		ClientInterfaceManager cim = test.create(foo, null);
		
		cim.invoke(Foo.class.getMethod("foo", (Class<?>[]) null), null);
		
		assertTrue(foo.invoked);
	}

	/**
	 * Test that it fails with no factory.
	 * 
	 * @throws Throwable
	 */
   @Test
	public void testNoFactory() throws Throwable {
		
		class MockFoo implements Foo {
			boolean invoked;
			
			public void foo() {
				invoked = true;
			}
		}
		MockFoo foo = new MockFoo();
			
		ClientInterfaceManagerFactory test = new ClientInterfaceManagerFactory(null);
		
		ClientInterfaceManager cim = test.create(foo, null);
		try {
			cim.invoke(Foo.class.getMethod("foo", (Class<?>[]) null), null);
			fail("No interface factory so should fail");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
		
		assertFalse(foo.invoked);
	}

	/**
	 * Test for Object
	 * 
	 * @throws Throwable
	 */
   @Test
	public void testForObject() throws Throwable {
				
		class MockFoo implements Foo {
			public void foo() {
			}
		}
		
		MockFoo foo = new MockFoo();

		class FooClientHandlerFactory extends MockClientInterfaceHandlerFactory<Foo> {
			public Foo createClientHandler(Foo proxy, ClientSideToolkit toolkit) {
				return proxy;
			};
			
			public Class<Foo> interfaceClass() {
				return Foo.class;
			};
		}
		
		class OClientHandlerFactory extends MockClientInterfaceHandlerFactory<Object> {
			public Object createClientHandler(final Object proxy, 
					ClientSideToolkit toolkit) {
				return new Object() {
					public String toString() {
						return "Test";
					}
				};
			};
			
			public Class<Object> interfaceClass() {
				return Object.class;
			};
		}
		
		ClientInterfaceManagerFactory test = new ClientInterfaceManagerFactory(
				new ClientInterfaceHandlerFactory[] { 
						new OClientHandlerFactory(), new FooClientHandlerFactory()
				});
		
		ClientInterfaceManager cim = test.create(foo, null); 
		
		Object result = cim.invoke(Object.class.getMethod("toString", (Class<?>[]) null), null);
		
		assertEquals("Test", result);
	}

}
