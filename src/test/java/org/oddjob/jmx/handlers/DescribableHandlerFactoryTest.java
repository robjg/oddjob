package org.oddjob.jmx.handlers;

import org.junit.Test;
import org.oddjob.Describable;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.DirectInvocationClientFactory;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.MockServerSession;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerSession;

import java.util.Map;

public class DescribableHandlerFactoryTest extends OjTestCase {

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
		
		ArooaSession session = new StandardArooaSession();
		
		@Override
		public ServerSession getServerSession() {
			return new MockServerSession() {
				@Override
				public ArooaSession getArooaSession() {
					return session;
				}
			};
		}		
	}

	public static class Apple {
		
		public String getColour() {
			return "red";
		}
		
		protected String getType() {
			return "unknown";
		}
	}
	
   @Test
	public void testAllOperations() {

		DescribableHandlerFactory test = new DescribableHandlerFactory();
		
		ClientInterfaceHandlerFactory<Describable> clientFactory =
				new DirectInvocationClientFactory<>(Describable.class);
		
		OurClientToolkit clientToolkit = new OurClientToolkit();
		OurServerToolkit serverToolkit = new OurServerToolkit();
		
		clientToolkit.serverHandler = test.createServerHandler(
				new Apple(), serverToolkit);
		
		Describable proxy = clientFactory.createClientHandler(null, clientToolkit);

		Map<String, String> results = proxy.describe();
		
		assertEquals(2, results.size());
		assertEquals("red", results.get("colour"));
		assertEquals(Apple.class.toString(), results.get("class"));
	}
	
}
