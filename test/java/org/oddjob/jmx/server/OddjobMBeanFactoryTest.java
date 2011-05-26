package org.oddjob.jmx.server;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.oddjob.arooa.registry.Address;
import org.oddjob.jmx.handlers.StructuralHandlerFactory;
import org.oddjob.jobs.structural.JobFolder;

public class OddjobMBeanFactoryTest extends TestCase {

	class OurServerContext extends MockServerContext {

		ServerInterfaceManagerFactory simf;
		
		@Override
		public ServerContext addChild(Object child)
				throws ServerLoopBackException {
			return this;
		}
		
		@Override
		public ServerModel getModel() {
			return new MockServerModel() {
				@Override
				public ServerInterfaceManagerFactory getInterfaceManagerFactory() {
					return simf;
				}
			};
		}
		
		@Override
		public Address getAddress() {
			return null;
		}
	}
	
	public void testStruture() throws JMException {
		
		JobFolder folder = new JobFolder();

		Object c1 = new Object();
		Object c2 = new Object();
		
		folder.setJobs(0, c1);
		folder.setJobs(1, c2);
		
		MBeanServer server = MBeanServerFactory.createMBeanServer();
	
		OddjobMBeanFactory test = new OddjobMBeanFactory(server);
		
		ServerInterfaceManagerFactoryImpl simf = 
			new ServerInterfaceManagerFactoryImpl(
					new ServerInterfaceHandlerFactory<?, ?>[] {
							new StructuralHandlerFactory()
					});
		
		OurServerContext context = new OurServerContext();
		context.simf = simf; 
			
		ObjectName root = test.createMBeanFor(folder, context);
		
		assertEquals(new Integer(4), server.getMBeanCount());

		assertEquals(folder, test.objectFor(OddjobMBeanFactory.objectName(0)));
		assertEquals(c1, test.objectFor(OddjobMBeanFactory.objectName(1)));
		assertEquals(c2, test.objectFor(OddjobMBeanFactory.objectName(2)));
		
		test.destroy(root);
		
		assertEquals(new Integer(1), server.getMBeanCount());
	}

}
