package org.oddjob.jmx.handlers;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.Loadable;
import org.oddjob.arooa.life.ClassLoaderClassResolver;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.HandlerFactoryProvider;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ResourceFactoryProvider;
import org.oddjob.jmx.server.ServerInterfaceManager;
import org.oddjob.jmx.server.ServerInterfaceManagerFactory;
import org.oddjob.jmx.server.ServerInterfaceManagerFactoryImpl;

public class LoadableHandlerFactoryTest extends OjTestCase {

	public class MyLoadable implements Loadable {
		boolean loaded;
		
		@Override
		public boolean isLoadable() {
			return !loaded;
		}
		
		@Override
		public void load() {
			loaded = true;
		}
		
		@Override
		public void unload() {
			loaded = false;
		}
	}
	
	private class OurClientToolkit extends MockClientSideToolkit {
		
		ServerInterfaceManager serverManager;
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
				throws Throwable {
			return (T) serverManager.invoke(
					remoteOperation.getActionName(), 
					args, 
					remoteOperation.getSignature());
		}
	}
	
   @Test
	public void testCreation() {
		
		HandlerFactoryProvider provider = 
			new ResourceFactoryProvider(new StandardArooaSession());

		ServerInterfaceManagerFactory managerFactory = 
			new ServerInterfaceManagerFactoryImpl(
					provider.getHandlerFactories());
		
		MyLoadable loadable = new MyLoadable();
		
		ServerInterfaceManager manager =
			managerFactory.create(loadable, new MockServerSideToolkit());
		
		ClientHandlerResolver<?>[] resolvers = manager.allClientInfo();
		
		assertEquals(1, resolvers.length);
	
		ClientInterfaceHandlerFactory<?> clientFactory = 
			resolvers[0].resolve(new ClassLoaderClassResolver(
				getClass().getClassLoader()));
		
		OurClientToolkit clientToolkit = new OurClientToolkit();
		clientToolkit.serverManager = manager;
		
		Loadable proxy = (Loadable) 
			clientFactory.createClientHandler(null, clientToolkit);
		
		assertEquals(true, proxy.isLoadable());
		
		proxy.load();
		
		assertEquals(false, proxy.isLoadable());
	}
}
