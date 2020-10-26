package org.oddjob.jmx.server;

import org.apache.commons.beanutils.DynaBean;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.Describable;
import org.oddjob.OjTestCase;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.MockClassResolver;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.framework.Exportable;
import org.oddjob.jmx.MockRemoteOddjobBean;
import org.oddjob.jmx.RemoteDirectoryOwner;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.client.LogPollable;
import org.oddjob.logging.LogEnabled;
import org.oddjob.remote.Implementation;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationType;
import org.oddjob.tools.OddjobTestHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        List<Notification<?>> sent = new ArrayList<>();

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
        public <T> Notification<T> createNotification(NotificationType<T> notificationType, T userData) {
            return new Notification<>(1L, notificationType, 0, userData);
        }

        @Override
        public void sendNotification(Notification<?> notification) {
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

        Implementation<?>[] interfaces =
                serverInterfaceManager.allClientInfo();

        String[] classNames = Arrays.stream(interfaces)
                .map(implementation -> implementation.getType())
                .toArray(String[]::new);

        assertThat(Arrays.asList(classNames), Matchers.containsInAnyOrder(
                Object.class.getName(),
                RemoteOddjobBean.class.getName(),
                RemoteDirectoryOwner.class.getName(),
                Structural.class.getName(),
                Describable.class.getName(),
                LogPollable.class.getName(),
                LogEnabled.class.getName(),
                DynaBean.class.getName(),
                Exportable.class.getName()));

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
