/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.jmx.server;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import junit.framework.TestCase;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;

public class InterfaceManagerImplTest extends TestCase {

	
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
					assertEquals("foo", operation.getActionName());
					return "Apples";
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
							MBeanOperationInfo.INFO) };
		}
		
		public Class<MockI> interfaceClass() {
			return MockI.class;
		}
		public ClientHandlerResolver<MockI> clientHandlerFactory() {
			return null;
		}
	}
	
	
	public void testInvoke() throws MBeanException, ReflectionException {
		MockI target = new MockI() {};
		
		ServerInterfaceManager test = new ServerInterfaceManagerImpl(
				target, null, new ServerInterfaceHandlerFactory[] { new MockInterfaceInfo() });

		Object result = test.invoke(
				"foo", 
				new Object[0], 
				new String[0]);
				
		assertEquals("Apples", result);
	}
	
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
