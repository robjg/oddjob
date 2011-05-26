package org.oddjob.jmx;

import junit.framework.TestCase;

import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryCrawler;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.MockBeanDirectoryOwner;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.Path;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.server.ServerInfo;

public class RemoteRegistryCrawlerTest extends TestCase {

	class ServerRegistry extends SimpleBeanRegistry
	implements RemoteDirectory {
		ServerId serverId;
		
		public ServerRegistry(ServerId serverId) {
			this.serverId = serverId;
		}
		
		public ServerId getServerId() {
			return serverId;
		}
	}
	
	class OurRemote extends MockRemoteOddjobBean {
		public ServerInfo serverInfo() {
			return new ServerInfo(
					new Address(new ServerId("server1"),
							new Path("a/b")),
					new ClientHandlerResolver[0]);
		}
	}

	public void testRemoteBean() {


		RemoteRegistryCrawler test = new RemoteRegistryCrawler(
				new MockBeanRegistry());
		
		OurRemote remote = new OurRemote();
		
		Address address = test.addressFor(remote);
		
		assertEquals("server1:a/b", address.toString());
	}
	
	/** Test a single local registry with no children. */
	public void testSingle() {
		Object comp = new Object();
		
		BeanRegistry cr = new SimpleBeanRegistry();
		
		cr.register("foo", comp);

		RemoteRegistryCrawler test = new RemoteRegistryCrawler(cr);
		
		// check it has an address
		Address address = test.addressFor(comp);
		assertNotNull(address);
		
		assertEquals(ServerId.local(), address.getServerId());
		assertEquals(new Path("foo"), address.getPath());
		
		// and we can look it up by that address.
		assertEquals(comp, test.objectForAddress(
						address));
	}

	class Component extends MockBeanDirectoryOwner {
		final String name;
		BeanDirectory directory;
		
		Component(String name) {
			this.name = name;
		}
		public String toString() {
			return name;
		}
		
		public BeanDirectory provideBeanDirectory() {
			return directory;
		}
	}
	
	/** Test a hierarchy for the same server */
	public void testSameServer() {
		Component comp1 = new Component("comp1");

		ServerRegistry cr1 = new ServerRegistry(new ServerId("server1"));
		cr1.register("a", comp1);

		ServerRegistry cr2 = new ServerRegistry(new ServerId("server1"));

		OurRemote comp2 = new OurRemote();

		comp1.directory = cr2;
		
		cr2.register("b", comp2);
		
		// check we can get object for path
		assertEquals(comp2, cr1.lookup("a/b"));
		
		RemoteRegistryCrawler test = new RemoteRegistryCrawler(cr1);
		
		// check there is an address
		Address address = test.addressFor(comp2);
		assertNotNull(address);

		// check the address
		assertEquals("server1:a/b", address.toString());
		
		// check we can get the object back for addresses.
		assertEquals(
				comp2, test.objectForAddress(
						address));
	}

	/** Test a hierarchy with a different server */
	public void testDifferentServer() {
		Component comp1 = new Component("comp1");

		ServerRegistry cr1 = new ServerRegistry(new ServerId("server1"));
		cr1.register("a", comp1);

		ServerRegistry cr2 = new ServerRegistry(new ServerId("server2"));

		comp1.directory = cr2;
		
		OurRemote comp2 = new OurRemote();

		cr2.register("b", comp2);

		// check we can get the path. 
		assertEquals("a/b", 
				new BeanDirectoryCrawler(cr1).pathForObject(comp2).toString());

		// check we can get the object by path
		assertEquals(comp2, cr1.lookup("a/b"));
		
		RemoteRegistryCrawler test = new RemoteRegistryCrawler(cr1);
		

		// check finding the second server
		RemoteDirectory checkCR = (RemoteDirectory) test.registryForServer(new ServerId("server2"));
		assertNotNull(checkCR);
		
		// check we get the component back for addresses
		assertEquals(
				comp2, test.objectForAddress(
						new Address(new ServerId("server2"), new Path("b"))));
		
	}
	
	/** Test a double sided owner object - has an id in both registries such
	 * as Oddjob */
	public void testTwoFaced() {
		Component comp1 = new Component("comp");

		ServerRegistry cr1 = new ServerRegistry(
				new ServerId("server1"));
		
		cr1.register("a", comp1);

		ServerRegistry cr2 = new ServerRegistry(
				new ServerId("server2"));

		comp1.directory = cr2;
		
		cr2.register("b", comp1);
		
		// check we get the shortest path. 
		assertEquals("a", 
				new BeanDirectoryCrawler(
						cr1).pathForObject(comp1).toString());
		
		assertEquals(comp1, cr1.lookup("a/b"));
		
		RemoteRegistryCrawler test = new RemoteRegistryCrawler(cr2);
		
		assertEquals(
				comp1, test.objectForAddress(
						new Address(new ServerId("server2"), new Path("b"))));
	}
	
	public void testNoServerFor() {
		
		Component comp1 = new Component("comp");

		SimpleBeanRegistry cr1 = new SimpleBeanRegistry();
		
		cr1.register("a", comp1);

		ServerRegistry cr2 = new ServerRegistry(
				new ServerId("server1"));

		comp1.directory = cr2;
		
		RemoteRegistryCrawler test = new RemoteRegistryCrawler(cr1);
		
		assertNull(test.registryForServer(
				new ServerId("server3")));
		
		assertEquals(cr1, test.registryForServer(
				ServerId.local()));
	}
}
