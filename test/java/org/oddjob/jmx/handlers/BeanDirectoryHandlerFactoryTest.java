package org.oddjob.jmx.handlers;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.MockBeanDirectoryOwner;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.jmx.RemoteDirectory;
import org.oddjob.jmx.RemoteDirectoryOwner;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientSession;
import org.oddjob.jmx.client.MockClientSession;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.MockServerContext;
import org.oddjob.jmx.server.MockServerSession;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ServerContext;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerSession;

public class BeanDirectoryHandlerFactoryTest extends OjTestCase {

	class ServerSideOwner1 extends MockBeanDirectoryOwner {
		
		public BeanDirectory provideBeanDirectory() {
			return null;
		}
	}
	
	class OurServerToolkit1 extends MockServerSideToolkit {
		
		@Override
		public ServerContext getContext() {
			return new MockServerContext() {
				@Override
				public ServerId getServerId() {
					return new ServerId("//Fish");
				}
			};
		}
	}
	
	class OurClientToolkit extends MockClientSideToolkit {

		ServerInterfaceHandler handler;
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
				throws Throwable {
			return (T) handler.invoke(
					remoteOperation,
					args);
		}
	}
	
   @Test
	public void testGetServerId() {
		
		ServerSideOwner1 target = new ServerSideOwner1();
		
		BeanDirectoryHandlerFactory test = new BeanDirectoryHandlerFactory();
		
		ServerInterfaceHandler serverHandler = test.createServerHandler(
				target, new OurServerToolkit1());
		
		OurClientToolkit clientToolkit = new OurClientToolkit();
		clientToolkit.handler = serverHandler;
		
		RemoteDirectoryOwner client =
			new BeanDirectoryHandlerFactory.ClientBeanDirectoryHandlerFactory(
					).createClientHandler(null, clientToolkit);
		
		RemoteDirectory remote = client.provideBeanDirectory();
		
		ServerId id = remote.getServerId();
		
		assertEquals("//Fish", id.toString());
	}
	
	class ServerSideOwner2 extends MockBeanDirectoryOwner {

		String lookup;
		
		public BeanDirectory provideBeanDirectory() {
			return new MockBeanRegistry() {
				@Override
				public Object lookup(String path) {
					lookup = path;
					return "Fish";
				}
			};
		}
	}
	
	class OurServerToolkit2 extends MockServerSideToolkit {
		
		@Override
		public ServerSession getServerSession() {
			return new MockServerSession() {
				@Override
				public ObjectName nameFor(Object object) {
					assertEquals("Fish", object);
					return null;
				}
			};
		}
	}
	
   @Test
	public void testLookup() {
		
		ServerSideOwner2 target = new ServerSideOwner2();
		
		BeanDirectoryHandlerFactory test = new BeanDirectoryHandlerFactory();
		
		ServerInterfaceHandler serverHandler = test.createServerHandler(
				target, new OurServerToolkit2());
		
		OurClientToolkit clientToolkit = new OurClientToolkit();
		clientToolkit.handler = serverHandler;
		
		RemoteDirectoryOwner client =  
			new BeanDirectoryHandlerFactory.ClientBeanDirectoryHandlerFactory(
					).createClientHandler(null, clientToolkit);
		
		SimpleBeanRegistry registry = new SimpleBeanRegistry();
		
		registry.register("snacks", client);
		
		Object result = registry.lookup("snacks/and/this/goes/accross/the.wire");
		
		assertEquals("Fish", result);
		
		assertEquals("and/this/goes/accross/the.wire", target.lookup);
	}
	
	class ServerSideOwner3 extends MockBeanDirectoryOwner {

		SimpleBeanRegistry registry = new SimpleBeanRegistry();
		
		{
			registry.register("x", "Dog");
		}
		
		public BeanDirectory provideBeanDirectory() {
			return new MockBeanRegistry() {
				@Override
				public <T> Iterable<T> getAllByType(Class<T> type) {
					return registry.getAllByType(type);
				}
			};
		}
	}
	
	ObjectName dogName;
	
	{
		try {
			dogName = new ObjectName("test", "test", "test"); 
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	class OurServerToolkit3 extends MockServerSideToolkit {
		
		@Override
		public ServerSession getServerSession() {
			return new MockServerSession() {
				@Override
				public ObjectName nameFor(Object object) {
					assertEquals("Dog", object);
					return dogName;
				}
			};
		}
	}
	
	class OurClientToolkit3  extends MockClientSideToolkit {
		
		ServerInterfaceHandler handler;
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
				throws Throwable {
			return (T) handler.invoke(
					remoteOperation,
					args);
		}
		
		@Override
		public ClientSession getClientSession() {
			return new MockClientSession() {
				@Override
				public Object create(ObjectName objectName) {
					assertEquals(dogName, objectName);
					return "Cat";
				}
			};
		}
	}
	
   @Test
	public void testGetAllByType() {
		
		ServerSideOwner3 target = new ServerSideOwner3();
		
		BeanDirectoryHandlerFactory test = new BeanDirectoryHandlerFactory();
		
		ServerInterfaceHandler serverHandler = test.createServerHandler(
				target, new OurServerToolkit3());
		
		OurClientToolkit3 clientToolkit = new OurClientToolkit3();
		clientToolkit.handler = serverHandler;
		
		RemoteDirectoryOwner client =  
			new BeanDirectoryHandlerFactory.ClientBeanDirectoryHandlerFactory(
					).createClientHandler(null, clientToolkit);
		
		Iterable<Object> iterable = 
			client.provideBeanDirectory().getAllByType(Object.class);

		List<Object> results = new ArrayList<Object>();
		
		for (Object o : iterable) {
			results.add(o);
		}
		
		assertEquals(1, results.size());
		assertEquals("Cat", results.get(0));
	}
}
