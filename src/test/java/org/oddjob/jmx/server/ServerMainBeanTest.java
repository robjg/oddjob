package org.oddjob.jmx.server;

import org.apache.commons.beanutils.DynaBean;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.Describable;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerMainBeanTest {

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
        public long getRemoteId() {
            return 42L;
        }

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
                    assertThat(ServerMainBeanTest.this.childName, is(childName));
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

        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(beanDirectory);

        ServerContextImpl context = new ServerContextImpl(
                test, model, parentContext);

        OurServerToolkit toolkit =
                new OurServerToolkit();
        toolkit.context = context;

        ServerInterfaceManager serverInterfaceManager =
                simf.create(test, toolkit);

        assertThat(toolkit.child, is(child));
        assertThat(toolkit.sent.size(), is(1));

        Implementation<?>[] interfaces =
                serverInterfaceManager.allClientInfo();

        String[] classNames = Arrays.stream(interfaces)
                .map(Implementation::getType)
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

        assertThat(toolkit.child, nullValue());
        assertThat(toolkit.sent.size(), is(2));
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

        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(beanDir);

        ServerContext context = new ServerContextImpl(
                test,
                model,
                parentContext);

        Object[] children = OddjobTestHelper.getChildren(test);

        assertThat(children.length, is(1));
        assertThat(children[0], is(root));

        ServerContext childContext = context.addChild(root);

        assertThat(model.getServerId(), is(childContext.getServerId()));
    }
}
