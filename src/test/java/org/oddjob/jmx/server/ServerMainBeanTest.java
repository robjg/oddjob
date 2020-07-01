package org.oddjob.jmx.server;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.MockClassResolver;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.MockRemoteOddjobBean;
import org.oddjob.jmx.RemoteDirectoryOwner;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.remote.Notification;
import org.oddjob.tools.OddjobTestHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerMainBeanTest extends OjTestCase {

    private static class OurModel extends MockServerModel {

        ServerInterfaceManagerFactory simf;

        @Override
        public ServerInterfaceManagerFactory getInterfaceManagerFactory() {
            return simf;
        }

        @Override
        public String getLogFormat() {
            return null;
        }

        @Override
        public ServerId getServerId() {
            return new ServerId("http://test");
        }
    }

    long childName = 2L;

    private class OurServerToolkit extends MockServerSideToolkit {

        ArooaSession session = new StandardArooaSession();

        Object child;

        List<Notification> sent = new ArrayList<>();

        ServerContext context;

        @Override
        public ServerContext getContext() {
            return context;
        }

        @Override
        public RemoteOddjobBean getRemoteBean() {
            return new MockRemoteOddjobBean();
        }

        @Override
        public void runSynchronized(Runnable runnable) {
            runnable.run();
        }

        @Override
        public Notification createNotification(String type, Object userData) {
            return new Notification(1L, "X", 0, userData);
        }

        @Override
        public void sendNotification(Notification notification) {
            sent.add(notification);
        }

        @Override
        public ServerSession getServerSession() {
            return new MockServerSession() {
                @Override
                public long createMBeanFor(Object theChild,
                                           ServerContext childContext) {
                    child = theChild;
                    return childName;
                }

                @Override
                public void destroy(long childName) {
                    assertEquals(ServerMainBeanTest.this.childName, childName);
                    child = null;
                }

                @Override
                public ArooaSession getArooaSession() {
                    return session;
                }
            };
        }

    }

    private static class OurClassResolver extends MockClassResolver {

        @Override
        public Class<?> findClass(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void testInterfaces() {

        BeanDirectory beanDirectory = new MockBeanRegistry() {
            @Override
            public String getIdFor(Object component) {
                return null;
            }
        };

        Object child = new Object();

        ServerMainBean test = new ServerMainBean(
                child,
                beanDirectory);

        ServerInterfaceManagerFactoryImpl simf =
                new ServerInterfaceManagerFactoryImpl();
        simf.addServerHandlerFactories(
                new ResourceFactoryProvider(
                        new StandardArooaSession()).getHandlerFactories());

        OurModel model = new OurModel();
        model.simf = simf;

        ServerContextImpl context = new ServerContextImpl(
                test, model, beanDirectory);

        OurServerToolkit toolkit =
                new OurServerToolkit();
        toolkit.context = context;

        ServerInterfaceManager serverInterfaceManager =
                simf.create(test, toolkit);

        assertEquals(child, toolkit.child);
        assertEquals(1, toolkit.sent.size());

        ClientHandlerResolver<?>[] clientFactories =
                serverInterfaceManager.allClientInfo();

        Set<Class<?>> interfaces = new HashSet<>();

        for (ClientHandlerResolver<?> clientFactory : clientFactories) {
            interfaces.add(clientFactory.resolve(
                    new OurClassResolver()).interfaceClass());
        }

        assertTrue(interfaces.contains(Object.class));
        assertTrue(interfaces.contains(RemoteOddjobBean.class));
        assertTrue(interfaces.contains(RemoteDirectoryOwner.class));
        assertTrue(interfaces.contains(Structural.class));

        serverInterfaceManager.destroy();

        assertNull(toolkit.child);
        assertEquals(2, toolkit.sent.size());
    }

    @Test
    public void testStructural() throws ServerLoopBackException {

        Object root = new Object();

        BeanDirectory beanDir = new MockBeanRegistry() {
            @Override
            public String getIdFor(Object component) {
                return null;
            }
        };

        ServerMainBean test = new ServerMainBean(
                root,
                beanDir);

        OurModel model = new OurModel();

        ServerContext context = new ServerContextImpl(
                test,
                model,
                beanDir);

        Object[] children = OddjobTestHelper.getChildren(test);

        assertEquals(1, children.length);
        assertEquals(root, children[0]);

        ServerContext childContext = context.addChild(root);

        assertEquals(model.getServerId(), childContext.getServerId());
    }
}
