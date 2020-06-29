package org.oddjob.jmx.server;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.registry.Address;
import org.oddjob.jmx.handlers.StructuralHandlerFactory;
import org.oddjob.jobs.structural.JobFolder;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

public class OddjobMBeanFactoryTest extends OjTestCase {

    private static class OurServerContext extends MockServerContext {

        ServerInterfaceManagerFactory simf;

        @Override
        public ServerContext addChild(Object child) {
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

    @Test
    public void testStructure() throws JMException {

        JobFolder folder = new JobFolder();

        Object c1 = new Object();
        Object c2 = new Object();

        folder.setJobs(0, c1);
        folder.setJobs(1, c2);

        MBeanServer server = MBeanServerFactory.createMBeanServer();

        OddjobMBeanFactory test = new OddjobMBeanFactory(server, null);

        ServerInterfaceManagerFactoryImpl simf =
                new ServerInterfaceManagerFactoryImpl(
                        new ServerInterfaceHandlerFactory<?, ?>[]{
                                new StructuralHandlerFactory()
                        });

        OurServerContext context = new OurServerContext();
        context.simf = simf;

        long root = test.createMBeanFor(folder, context);

        assertEquals(new Integer(4), server.getMBeanCount());

        assertEquals(folder, test.objectFor(0L));
        assertEquals(c1, test.objectFor(1L));
        assertEquals(c2, test.objectFor(2));

        test.destroy(root);

        assertEquals(new Integer(1), server.getMBeanCount());
    }

}
