/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.InvalidIdException;
import org.oddjob.arooa.registry.MockBeanDirectoryOwner;
import org.oddjob.arooa.registry.Path;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.jmx.RemoteDirectory;
import org.oddjob.jmx.RemoteDirectoryOwner;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogListener;
import org.oddjob.util.MockThreadManager;

public class ServerContextImplTest extends OjTestCase {

	
   @Test
	public void testSimple() throws InvalidIdException {
		Object comp = new Object();
		SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
		cr1.register("foo", comp);
		
		ServerModel sm = new ServerModelImpl(
				new ServerId("//test"), 
				new MockThreadManager(), 
				new MockServerInterfaceManagerFactory());
		
		ServerContext test = new ServerContextImpl(
				comp, sm, cr1);
		
		assertEquals(sm, test.getModel());
		
		assertEquals(new Address(new ServerId("//test"), new Path("foo")), 
				test.getAddress());
	}

	class OurOwner extends MockBeanDirectoryOwner {
		BeanDirectory beanDirectory;
		
		public BeanDirectory provideBeanDirectory() {
			return beanDirectory;
		}
	}
	
	// registry when the top node is a registry owner.
   @Test
	public void testTopChildRegistry() throws ServerLoopBackException {
		OurOwner client = new OurOwner();
		
		SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
		cr1.register("client", client);

		SimpleBeanRegistry cr2 = new SimpleBeanRegistry();
		client.beanDirectory = cr2;
				
		
		Object node = new Object();
		cr2.register("foo", node);
				
		ServerModel sm = new ServerModelImpl(
				new ServerId("//test"), 
				new MockThreadManager(), 
				new MockServerInterfaceManagerFactory());
		
		ServerContext sc1 = new ServerContextImpl(
				client, sm, cr1);

		Address address1 = new Address(
				new ServerId("//test"), new Path("client"));
		
		assertEquals(address1, sc1.getAddress());
		
		ServerContext sc2 = sc1.addChild(node);
		assertTrue(sc1.getBeanDirectory() != sc2.getBeanDirectory());
		
		Address address = new Address(
				new ServerId("//test"), new Path("client/foo"));
		
		assertEquals(address, sc2.getAddress());
	}

	// registry when the second node is a registry owner.
   @Test
	public void testChildRegistry() throws ServerLoopBackException {
		Object top = new Object();
		
		SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
		cr1.register("top", top);

		OurOwner node = new OurOwner();
		cr1.register("foo", node);
		
		SimpleBeanRegistry cr2 = new SimpleBeanRegistry();
		node.beanDirectory = cr2;
		
		ServerModel sm = new ServerModelImpl(
				new ServerId("//test"), 
				new MockThreadManager(), 
				new MockServerInterfaceManagerFactory());
		
		ServerContext sc1 = new ServerContextImpl(
				top, sm, cr1);
		
		ServerContext sc2 = sc1.addChild(node);
		
		Object inner = new Object();
		cr2.register("inner", inner);
		// sc2 is only a parent when it needs to be
		ServerContext sc3 = sc2.addChild(inner);
		
		assertEquals(
				new Address(new ServerId("//test"), new Path("foo/inner")),
				sc3.getAddress());
	}

	// registry when the second node is a registry owner.
   @Test
	public void testChildRegistryNoPath() throws ServerLoopBackException {
		SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
		
		OurOwner top = new OurOwner();
		
		SimpleBeanRegistry cr2 = new SimpleBeanRegistry();
		top.beanDirectory = cr2;
		
		OurOwner node = new OurOwner();
		
		cr2.register("apples", node);
		
		SimpleBeanRegistry cr3 = new SimpleBeanRegistry();
		node.beanDirectory = cr3;
		
		Object inner = new Object();
		cr3.register("inner", inner);
		
		ServerModel sm = new ServerModelImpl(
				new ServerId("//test"), 
				new MockThreadManager(), 
				new MockServerInterfaceManagerFactory());
		
		ServerContext sc1 = new ServerContextImpl(
				top, sm, cr1);
		
		ServerContext sc2 = sc1.addChild(node);
		
		ServerContext sc3 = sc2.addChild(inner);
		
		assertNull(sc3.getAddress());
	}

	class OurRemote extends MockBeanDirectoryOwner 
	implements RemoteDirectoryOwner {
		BeanDirectory beanDirectory;
		
		ServerId serverId;
		
		public RemoteDirectory provideBeanDirectory() {
			return new RemoteDirectory() {

				public ServerId getServerId() {
					return serverId;
				}

				public <T> Iterable<T> getAllByType(Class<T> type) {
					return beanDirectory.getAllByType(type);
				}

				public String getIdFor(Object bean) {
					return beanDirectory.getIdFor(bean);
				}

				public Object lookup(String path) {
					return beanDirectory.lookup(path);
				}

				public <T> T lookup(String path, Class<T> required)
						throws ArooaConversionException {
					return lookup(path, required);
				}
				
			};
		}
	}
	
   @Test
	public void testDifferentServers() throws ServerLoopBackException {
		OurRemote top = new OurRemote();
		SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
		cr1.register("top", top);

		SimpleBeanRegistry cr2 = new SimpleBeanRegistry();
		top.serverId = new ServerId("//toast");
		top.beanDirectory = cr2;
		
		Object inner = new Object();
		cr2.register("inner", inner);
		
		ServerModel sm = new ServerModelImpl(
				new ServerId("//test"), 
				new MockThreadManager(), 
				new MockServerInterfaceManagerFactory());
		
		ServerContext sc1 = new ServerContextImpl(
				top, sm, cr1);
		
		Address address1 = new Address(
				new ServerId("//test"), new Path("top"));
		assertEquals(address1, sc1.getAddress());
		
		ServerContext sc2 = sc1.addChild(inner);

		Address address2 = new Address(
				new ServerId("//toast"), new Path("inner")); 
		assertEquals(address2, sc2.getAddress());
	}
	
   @Test
	public void testDuplicateServers() throws ServerLoopBackException {
		OurRemote top = new OurRemote();
		SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
		cr1.register("top", top);

		SimpleBeanRegistry cr2 = new SimpleBeanRegistry();
		top.serverId = new ServerId("//test");
		top.beanDirectory = cr2;
		
		Object inner = new Object();
		cr2.register("inner", inner);
		
		ServerModel sm = new ServerModelImpl(
				new ServerId("//test"), 
				new MockThreadManager(), 
				new MockServerInterfaceManagerFactory());
		
		ServerContext sc1 = new ServerContextImpl(
				top, sm, cr1);
		
		Address address1 = new Address(
				new ServerId("//test"), new Path("top"));
		assertEquals(address1, sc1.getAddress());

		try {
			sc1.addChild(inner);
			fail("Shoul fail.");
		} catch (ServerLoopBackException e) {
			// expected.
		}
		
	}
			
   @Test
	public void testLogArchiver() throws ServerLoopBackException {
		
		final Object node = new Object();
		
		class OurArchiver implements LogArchiver {
			public void addLogListener(LogListener l, Object component,
					LogLevel level, long last, int max) {
				throw new RuntimeException("Unexpected.");
			}
			public void removeLogListener(LogListener l, Object component) {
				throw new RuntimeException("Unexpected.");
			}
		}
		
		SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
		
		OurArchiver top = new OurArchiver();
		cr1.register("top", top);
		
		cr1.register("foo", node);
				
		ServerModel sm = new ServerModelImpl(
				new ServerId("//test"), 
				new MockThreadManager(), 
				new MockServerInterfaceManagerFactory());
				
		ServerContext sc1 = new ServerContextImpl(top, sm, cr1);
		
		ServerContext sc2 = sc1.addChild(node);

		assertEquals(top, sc2.getLogArchiver());
	}
	
}
