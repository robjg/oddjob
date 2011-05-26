package org.oddjob.jmx.handlers;

import java.util.Map;

import junit.framework.TestCase;

import org.oddjob.Describeable;
import org.oddjob.arooa.life.ClassLoaderClassResolver;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ServerInterfaceHandler;

public class DescribeableHandlerFactoryTest extends TestCase {

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
	}

	public class Apple {
		
		public String getColour() {
			return "red";
		}
		
		protected String getType() {
			return "unknown";
		}
	}
	
	public void testAllOperations() {

		DescribeableHandlerFactory test = new DescribeableHandlerFactory();
		
		ClientHandlerResolver<Describeable> resolver = test.clientHandlerFactory();
		ClientInterfaceHandlerFactory<Describeable> clientFactory =
			resolver.resolve(new ClassLoaderClassResolver(getClass().getClassLoader()));
		
		OurClientToolkit clientToolkit = new OurClientToolkit();
		OurServerToolkit serverToolkit = new OurServerToolkit();
		
		clientToolkit.serverHandler = test.createServerHandler(
				new Apple(), serverToolkit);
		
		Describeable proxy = clientFactory.createClientHandler(null, clientToolkit);

		Map<String, String> results = proxy.describe();
		
		assertEquals(2, results.size());
		assertEquals("red", results.get("colour"));
		assertEquals(Apple.class.toString(), results.get("class"));
	}
	
}
