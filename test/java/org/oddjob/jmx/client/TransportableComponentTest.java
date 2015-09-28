/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.jmx.client;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.oddjob.OddjobConsole;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ClassResolver;
import org.oddjob.arooa.MockArooaDescriptor;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.MockClassResolver;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.registry.SimpleBeanRegistry;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.handlers.StructuralHandlerFactory;
import org.oddjob.jmx.server.OddjobMBeanFactory;
import org.oddjob.jmx.server.ServerContext;
import org.oddjob.jmx.server.ServerContextImpl;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerInterfaceManagerFactoryImpl;
import org.oddjob.jmx.server.ServerModel;
import org.oddjob.jmx.server.ServerModelImpl;
import org.oddjob.jobs.structural.JobFolder;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.util.MockThreadManager;

/**
 *
 */
public class TransportableComponentTest extends TestCase {
	private static final Logger logger = Logger.getLogger(TransportableComponentTest.class);

	
	protected void setUp() {
		logger.debug("================== Running " + getName() + "================");
		System.setProperty("mx4j.log.priority", "trace");
	}
			
	public static class MyComponent {
		MyComponent another;
		public void setAnother(MyComponent another) {
			this.another = another;
		}
		public MyComponent getAnother() {
			return another;
		}
		public Object getAnotherReally() throws MalformedObjectNameException, NullPointerException {
			return new ComponentTransportable(OddjobMBeanFactory.objectName(2));
		}
	}
	
	private class OurArooaSession extends MockArooaSession {
		@Override
		public ArooaDescriptor getArooaDescriptor() {
			return new MockArooaDescriptor() {
				@Override
				public ClassResolver getClassResolver() {
					return new MockClassResolver() {
						@Override
						public Class<?> findClass(String className) {
							try {
								return Class.forName(className);
							} catch (ClassNotFoundException e) {
								throw new RuntimeException(e);
							}
						}
					};
				}
			};
		}
		
	}
	
	/**
	 * Test a component can have another component set as it's property
	 * and the component can also be retrieved.
	 * 
	 * @throws Exception
	 */
	public void testRoundTrip() throws Exception {
		
		try (OddjobConsole.Close close = OddjobConsole.initialise()) {
			
			// simulate a server side with two components in a
			// folder.
			
			MyComponent c1 = new MyComponent();
			MyComponent c2 = new MyComponent();
			
			JobFolder folder = new JobFolder();
			folder.setJobs(0, c1);
			folder.setJobs(0, c2);
	
			ServerInterfaceManagerFactoryImpl imf = 
				new ServerInterfaceManagerFactoryImpl();
			
			imf.addServerHandlerFactories(
					new ServerInterfaceHandlerFactory<?, ?>[] {
						new StructuralHandlerFactory()	
					});
			
			ServerModel sm = new ServerModelImpl(
					new ServerId("//whatever"), 
					new MockThreadManager(), 
					imf);
			
			ServerContext serverContext = new ServerContextImpl(
					folder, sm, new SimpleBeanRegistry());
			
			MBeanServer mbs = MBeanServerFactory.createMBeanServer();
				
			OddjobMBeanFactory factory = new OddjobMBeanFactory(mbs, 
					new StandardArooaSession());
			ObjectName on = factory.createMBeanFor(folder, serverContext); 
			
			// client side.
			ClientSession clientSession = new ClientSessionImpl(				
					mbs, 
					new DummyNotificationProcessor(),
					new OurArooaSession(),
					logger);
			
			Object folderProxy = clientSession.create(on);
			
			assertNotNull(folderProxy);
			
			Object[] children = OddjobTestHelper.getChildren((Structural) folderProxy);
			
			Object c1Proxy = children[0];
			assertNotNull(c1Proxy);
			Object c2Proxy = children[1];
			assertNotNull(c2Proxy);
			
			PropertyUtils.setProperty(c1Proxy, "another", c2Proxy);
			
			Object result = PropertyUtils.getProperty(c1Proxy, "anotherReally");
			
			assertEquals(c2Proxy, result);
		}
	}
	
}
