/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.jmx.server;

import org.junit.Test;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import org.oddjob.OjTestCase;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;

public class InterfaceManagerImplTest extends OjTestCase {

	
	interface MockI {
		
	}
	
	class MockInterfaceInfo implements ServerInterfaceHandlerFactory<MockI, MockI> {
		boolean destroyed;
		
		public ServerInterfaceHandler createServerHandler(MockI target, ServerSideToolkit ojmb) {
			return new ServerInterfaceHandler() {
				public void destroy() {
					destroyed = true;
				}
				public Object invoke(RemoteOperation<?> operation, Object[] params) 
				throws MBeanException, ReflectionException {
					if ("foo".equals(operation.getActionName())) {
						return "Apples";
					}
					else if ("moo".equals(operation.getActionName()))
					{
						return "Oranges";
					}
					else throw new RuntimeException("Unexpected!");
				}
				
			};
		}
		public MBeanAttributeInfo[] getMBeanAttributeInfo() {
			return new MBeanAttributeInfo[0];
		}
		public MBeanNotificationInfo[] getMBeanNotificationInfo() {
			return new MBeanNotificationInfo[0];
		}
		public MBeanOperationInfo[] getMBeanOperationInfo() {
			return new MBeanOperationInfo[] {
					new MBeanOperationInfo(
							"foo", 
							"Foo method",
							new MBeanParameterInfo[0],
							String.class.getName(),
							MBeanOperationInfo.INFO), 
					new MBeanOperationInfo(
							"moo", 
							"Moo method",
							new MBeanParameterInfo[0],
							String.class.getName(),
							MBeanOperationInfo.ACTION_INFO)};
		}
		
		public Class<MockI> interfaceClass() {
			return MockI.class;
		}
		public ClientHandlerResolver<MockI> clientHandlerFactory() {
			return null;
		}
	}
	
   @Test
	public void testAllClientInfo() throws MBeanException, ReflectionException {
		MockI target = new MockI() {};
		
		ServerInterfaceManager test = new ServerInterfaceManagerImpl(
				target, null, new ServerInterfaceHandlerFactory[] { new MockInterfaceInfo() });

		ClientHandlerResolver<?>[] result = test.allClientInfo();
				
		assertEquals(1, result.length);
	}
	
   @Test
	public void testAllClientInfoReadOnly() throws MBeanException, ReflectionException {
		MockI target = new MockI() {};
		
		ServerInterfaceManager test = new ServerInterfaceManagerImpl(
				target, null, new ServerInterfaceHandlerFactory[] { new MockInterfaceInfo() },
				new OddjobJMXAccessController() {
					
					@Override
					public boolean isAccessable(MBeanOperationInfo opInfo) {
						return opInfo.getImpact() == MBeanOperationInfo.INFO;
					}
				});

		ClientHandlerResolver<?>[] result = test.allClientInfo();
				
		assertEquals(0, result.length);
	}
	
   @Test
	public void testInvoke() throws MBeanException, ReflectionException {
		MockI target = new MockI() {};
		
		ServerInterfaceManager test = new ServerInterfaceManagerImpl(
				target, null, new ServerInterfaceHandlerFactory[] { new MockInterfaceInfo() });

		Object result = test.invoke(
				"foo", 
				new Object[0], 
				new String[0]);
				
		assertEquals("Apples", result);
		
		result = test.invoke(
				"moo", 
				new Object[0], 
				new String[0]);
				
		assertEquals("Oranges", result);
	}
	
   @Test
	public void testInvokeWithAccessController() throws MBeanException, ReflectionException {
		MockI target = new MockI() {};
		
		ServerInterfaceManager test = new ServerInterfaceManagerImpl(
				target, null, new ServerInterfaceHandlerFactory[] { new MockInterfaceInfo() },
				new OddjobJMXAccessController() {
					
					@Override
					public boolean isAccessable(MBeanOperationInfo opInfo) {
						return opInfo.getImpact() == MBeanOperationInfo.INFO;
					}
				});

		Object result = test.invoke(
				"foo", 
				new Object[0], 
				new String[0]);
				
		assertEquals("Apples", result);
		
		try {
			test.invoke(
					"moo", 
					new Object[0], 
					new String[0]);
			fail("Moo should fail!");
		}
		catch (SecurityException e) {
			// expected
		}
	}
	
   @Test
	public void testDestory() {
		MockI target = new MockI() {};
		
		MockInterfaceInfo factory = 
			new MockInterfaceInfo();
		
		ServerInterfaceManager test = new ServerInterfaceManagerImpl(
				target, null, new ServerInterfaceHandlerFactory[] { factory });

		test.destroy();
				
		assertTrue(factory.destroyed);
	}
}
