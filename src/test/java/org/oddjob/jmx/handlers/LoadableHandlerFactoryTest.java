package org.oddjob.jmx.handlers;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.Loadable;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.DirectInvocationClientFactory;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.*;
import org.oddjob.remote.Implementation;

import java.util.Arrays;

public class LoadableHandlerFactoryTest extends OjTestCase {

	public static class MyLoadable implements Loadable {
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
	
	private static class OurClientToolkit extends MockClientSideToolkit {
		
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
		
		Implementation<?>[] implementations = manager.allClientInfo();

	   String[] implClasses = Arrays.stream(implementations).map(Implementation::getType)
			   .toArray(String[]::new);

		assertThat(implClasses, Matchers.is(new String[] { Loadable.class.getName() }));

	   ClientInterfaceHandlerFactory<?> clientFactory =
			   new DirectInvocationClientFactory<>(Loadable.class);

		OurClientToolkit clientToolkit = new OurClientToolkit();
		clientToolkit.serverManager = manager;
		
		Loadable proxy = (Loadable) 
			clientFactory.createClientHandler(null, clientToolkit);

	   assertTrue(proxy.isLoadable());
		
		proxy.load();

	   assertFalse(proxy.isLoadable());
	}
}
