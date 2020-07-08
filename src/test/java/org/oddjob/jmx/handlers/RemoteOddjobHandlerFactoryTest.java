package org.oddjob.jmx.handlers;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.DirectInvocationClientFactory;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ServerInfo;
import org.oddjob.jmx.server.ServerInterfaceHandler;

import javax.management.MBeanOperationInfo;

public class RemoteOddjobHandlerFactoryTest extends OjTestCase {

	private static class OurClientToolkit extends MockClientSideToolkit {
		
		ServerInterfaceHandler serverHandler;
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
				throws Throwable {
			return (T) serverHandler.invoke(remoteOperation, args);
		}
	}
	
	private static class OurServerToolkit extends MockServerSideToolkit {
		boolean noop;
		
		@Override
		public RemoteOddjobBean getRemoteBean() {
			return new RemoteOddjobBean() {
				
				public ServerInfo serverInfo() {
					return null;
				}
				
				public void noop() {
					noop = true;
				}
			};
		}
	}
	
   @Test
	public void testAllOperations() {

		RemoteOddjobHandlerFactory test = new RemoteOddjobHandlerFactory();
		
		assertThat(test.clientClass(), Matchers.is(RemoteOddjobBean.class));

		ClientInterfaceHandlerFactory<RemoteOddjobBean> clientFactory =
				new DirectInvocationClientFactory<>(RemoteOddjobBean.class);

		OurClientToolkit clientToolkit = new OurClientToolkit();
		OurServerToolkit serverToolkit = new OurServerToolkit();
		
		clientToolkit.serverHandler = test.createServerHandler(
				null, serverToolkit);
		
		RemoteOddjobBean proxy = clientFactory.createClientHandler(null, clientToolkit);
		
		assertFalse(serverToolkit.noop);
		
		proxy.noop();
		
		assertTrue(serverToolkit.noop);
		
		ServerInfo serverInfo = proxy.serverInfo();
		
		assertNull(serverInfo);
	}
	
   @Test
	public void testOperationInfo() {
		
		RemoteOddjobHandlerFactory test = new RemoteOddjobHandlerFactory();
		
		MBeanOperationInfo[] opInfo = test.getMBeanOperationInfo();
		
		assertEquals(2, opInfo.length);
		
		MBeanOperationInfo opInfo0 = opInfo[0];
		
		assertEquals("serverInfo", 
				opInfo0.getName() );
	}
}
