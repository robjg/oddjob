package org.oddjob.jmx.handlers;

import org.junit.Test;

import javax.management.MBeanOperationInfo;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.life.ClassLoaderClassResolver;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ServerInfo;
import org.oddjob.jmx.server.ServerInterfaceHandler;

public class RemoteOddjobHandlerFactoryTest extends OjTestCase {

	private class OurClientToolkit extends MockClientSideToolkit {
		
		ServerInterfaceHandler serverHandler;
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
				throws Throwable {
			return (T) serverHandler.invoke(remoteOperation, args);
		}
	}
	
	private class OurServerToolkit extends MockServerSideToolkit {
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
		
		ClientHandlerResolver<RemoteOddjobBean> resolver = test.clientHandlerFactory();
		ClientInterfaceHandlerFactory<RemoteOddjobBean> clientFactory =
			resolver.resolve(new ClassLoaderClassResolver(getClass().getClassLoader()));
		
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
