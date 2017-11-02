package org.oddjob.jmx.server;

import org.junit.Test;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;

import org.oddjob.OjTestCase;

import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.MockClientHandlerResolver;
import org.oddjob.tools.OddjobTestHelper;

public class OddjobMBeanToolkitTest extends OjTestCase {

	private class OurServerContext extends MockServerContext {
		OurSIMF simf = new OurSIMF();

		@Override
		public ServerModel getModel() {
			return new MockServerModel() {
				@Override
				public ServerInterfaceManagerFactory getInterfaceManagerFactory() {
					return new ServerInterfaceManagerFactoryImpl(
							new ServerInterfaceHandlerFactory<?, ?>[] {
									simf
							});
				}
			};
		}
	}

	private interface Gold {
		
	}
	
	private class OurSIMF extends MockServerInterfaceHandlerFactory<Object, Gold> {
	
		ServerSideToolkit toolkit;
		
		@Override
		public ServerInterfaceHandler createServerHandler(Object target,
				ServerSideToolkit toolkit) {
			this.toolkit = toolkit;
			return new MockServerInterfaceHandler();
		}
		
		@Override
		public Class<Object> interfaceClass() {
			return Object.class;
		}
		
		@Override
		public ClientHandlerResolver<Gold> clientHandlerFactory() {
			return new MockClientHandlerResolver<Gold>();
		}
		
		@Override
		public MBeanAttributeInfo[] getMBeanAttributeInfo() {
			return new MBeanAttributeInfo[0];
		}
		
		@Override
		public MBeanNotificationInfo[] getMBeanNotificationInfo() {
			return new MBeanNotificationInfo[0];
		}
		
		@Override
		public MBeanOperationInfo[] getMBeanOperationInfo() {
			return new MBeanOperationInfo[0];
		}
	}
	
   @Test
	public void testNotification() throws Exception {
		
		Object node = new Object();

		OurServerContext context = new OurServerContext();
		
		new OddjobMBean(node, OddjobMBeanFactory.objectName(0), null, context);

		ServerSideToolkit toolkit = context.simf.toolkit;
		
		Notification n = toolkit.createNotification("X");
		
		assertEquals(0, n.getSequenceNumber());
		assertEquals(OddjobMBeanFactory.objectName(0), n.getSource());
		
		// test serializable.
		OddjobTestHelper.copy(n);
	}
}
