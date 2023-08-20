package org.oddjob.jmx.server;

import org.junit.Test;
import org.oddjob.arooa.registry.Address;
import org.oddjob.jmx.handlers.StructuralHandlerFactory;
import org.oddjob.jobs.structural.JobFolder;
import org.oddjob.remote.RemoteException;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OddjobMBeanFactoryTest {

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
    public void testStructure() throws RemoteException {

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

        assertThat(server.getMBeanCount(), is(4));

        assertThat(test.objectFor(0L), is(folder));
        assertThat(test.objectFor(1L), is(c1));
        assertThat(test.objectFor(2), is(c2));

        test.destroy(root);

        assertThat(server.getMBeanCount(), is(1));
    }

}
