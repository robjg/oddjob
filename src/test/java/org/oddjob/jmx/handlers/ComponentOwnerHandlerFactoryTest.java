package org.oddjob.jmx.handlers;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.oddjob.Oddjob;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.parsing.*;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.standard.StandardArooaDescriptor;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.matchers.CompareMatcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ComponentOwnerHandlerFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger(ComponentOwnerHandlerFactoryTest.class);

    private static class MySessionLite extends MockConfigurationSession {

        Object component;

        boolean cut;

        int pasteIndex;

        String pasteText;

        boolean committed;

        boolean saved;

        @Override
        public DragPoint dragPointFor(Object component) {

            this.component = component;

            return new MockDragPoint() {

                @Override
                public DragTransaction beginChange(ChangeHow how) {
                    return new DragTransaction() {

                        @Override
                        public void rollback() {
                        }

                        @Override
                        public void commit() {
                            committed = true;
                        }
                    };
                }

                @Override
                public boolean supportsCut() {
                    return true;
                }

                @Override
                public boolean supportsPaste() {
                    return true;
                }

                @Override
                public String copy() {
                    return "apples";
                }

                @Override
                public void delete() {
                    cut = true;
                }

                @Override
                public void paste(int index, String config) {
                    pasteIndex = index;
                    pasteText = config;
                }
            };
        }

        public void save() {
            saved = true;
        }

        @Override
        public void addSessionStateListener(SessionStateListener listener) {
        }

        @Override
        public void removeSessionStateListener(SessionStateListener listener) {
        }
    }

    private static class OurDesignFactory implements SerializableDesignFactory {
        private static final long serialVersionUID = 1L;

        @Override
        public DesignInstance createDesign(ArooaElement element,
                                           ArooaContext parentContext) throws ArooaPropertyException {
            throw new RuntimeException("Unexpected!");
        }
    }

    private static class MyComponentOwner extends MockConfigurationOwner {

        MySessionLite session = new MySessionLite();

        public ConfigurationSession provideConfigurationSession() {
            return session;
        }

        @Override
        public void addOwnerStateListener(OwnerStateListener listener) {
        }

        @Override
        public void removeOwnerStateListener(OwnerStateListener listener) {
        }

        @Override
        public ArooaElement rootElement() {
            return new ArooaElement("test");
        }

        @Override
        public SerializableDesignFactory rootDesignFactory() {
            return new OurDesignFactory();
        }
    }

    private static class OurServerSideToolkit extends MockServerSideToolkit {


    }

    private static class OurClientToolkit extends MockClientSideToolkit {

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
    public void testBasicInfo() {

        ComponentOwnerHandlerFactory test = new ComponentOwnerHandlerFactory();

        MyComponentOwner compO = new MyComponentOwner();

        ServerInterfaceHandler serverHandler = test.createServerHandler(
                compO, new OurServerSideToolkit());

        OurClientToolkit clientToolkit = new OurClientToolkit();
        clientToolkit.handler = serverHandler;

        ClientInterfaceHandlerFactory<ConfigurationOwner> cihf =
                new ComponentOwnerHandlerFactory.ClientFactory();

        ConfigurationOwner clientHandler = cihf.createClientHandler(
                new MockConfigurationOwner(), clientToolkit);

        assertEquals(new ArooaElement("test"),
                clientHandler.rootElement());

        assertEquals(OurDesignFactory.class,
                clientHandler.rootDesignFactory().getClass());
    }

    @Test
    public void testDragPointOperations() throws ArooaParseException {

        ComponentOwnerHandlerFactory test = new ComponentOwnerHandlerFactory();

        MyComponentOwner compO = new MyComponentOwner();

        ServerInterfaceHandler serverHandler = test.createServerHandler(
                compO, new OurServerSideToolkit());

        OurClientToolkit clientToolkit = new OurClientToolkit();
        clientToolkit.handler = serverHandler;

        ConfigurationOwner clientHandler =
                new ComponentOwnerHandlerFactory.ClientFactory(
                ).createClientHandler(new MockConfigurationOwner(), clientToolkit);

        Object ourComponent = new Object();

        DragPoint local = clientHandler.provideConfigurationSession().dragPointFor(
                ourComponent);

        assertTrue(local.supportsCut());
        assertTrue(local.supportsPaste());

        assertSame(ourComponent, compO.session.component);

        DragTransaction trn = local.beginChange(ChangeHow.FRESH);
        local.delete();
        trn.commit();

        assertTrue(compO.session.committed);
        assertTrue(compO.session.cut);

        assertEquals("apples", local.copy());

        DragTransaction trn2 = local.beginChange(ChangeHow.FRESH);
        local.paste(2, "oranges");
        trn2.commit();

        assertEquals(2, compO.session.pasteIndex);
        assertEquals("oranges", compO.session.pasteText);

        clientHandler.provideConfigurationSession().save();
        assertTrue(compO.session.saved);

    }


    private static class OurComponentOwner2 extends MockConfigurationOwner {

        private final ArooaDescriptor descriptor = new StandardArooaDescriptor();

        DragPoint drag;
        ConfigurationHandle<ArooaContext> handle;

        public ConfigurationSession provideConfigurationSession() {
            return new MockConfigurationSession() {
                @Override
                public DragPoint dragPointFor(Object component) {
                    return drag;
                }

                @Override
                public void save() throws ArooaParseException {
                    handle.save();
                }

                @Override
                public void addSessionStateListener(
                        SessionStateListener listener) {
                }

                @Override
                public void removeSessionStateListener(
                        SessionStateListener listener) {
                }

                @Override
                public ArooaDescriptor getArooaDescriptor() {
                    return descriptor;
                }
            };
        }

        @Override
        public void addOwnerStateListener(OwnerStateListener listener) {
        }

        @Override
        public void removeOwnerStateListener(OwnerStateListener listener) {
        }

        @Override
        public ArooaElement rootElement() {
            return new ArooaElement("test");
        }

        @Override
        public SerializableDesignFactory rootDesignFactory() {
            return new OurDesignFactory();
        }
    }

    @Test
    public void testEditOperations() throws Exception {


        Object root = new Object();

        XMLConfiguration config = new XMLConfiguration("TEST",
                "<class id='apples'/>");

        final AtomicReference<String> savedXML = new AtomicReference<>();
        config.setSaveHandler(savedXML::set);

        StandardArooaParser parser = new StandardArooaParser(root);

        final ConfigurationHandle<ArooaContext> handle = parser.parse(config);

        DragContext drag = new DragContext(handle.getDocumentContext());

        ComponentOwnerHandlerFactory test = new ComponentOwnerHandlerFactory();

        OurComponentOwner2 compO = new OurComponentOwner2();
        compO.drag = drag;
        compO.handle = handle;

        ServerInterfaceHandler serverHandler = test.createServerHandler(
                compO, new OurServerSideToolkit());

        OurClientToolkit clientToolkit = new OurClientToolkit();
        clientToolkit.handler = serverHandler;

        ConfigurationOwner clientHandler =
                new ComponentOwnerHandlerFactory.ClientFactory(
                ).createClientHandler(new MockConfigurationOwner(), clientToolkit);

        ConfigurationSession configurationSession = clientHandler.provideConfigurationSession();

        DragPoint local = configurationSession.dragPointFor(root);

        XMLArooaParser parser2 = new XMLArooaParser(NamespaceMappings.empty());

        ConfigurationHandle<SimpleParseContext> handle2 = parser2.parse(local);

        SimpleParseContext context = handle2.getDocumentContext();

        XMLConfiguration replacement = new XMLConfiguration("TEST",
                "<class id='oranges'/>");

        CutAndPasteSupport.replace(context.getParent(),
                context, replacement);
        handle2.save();

        clientHandler.provideConfigurationSession().save();

        String expected = "<class id=\"oranges\"/>" +
                System.getProperty("line.separator");

        MatcherAssert.assertThat(savedXML.get(), CompareMatcher.isSimilarTo(expected));
    }

    private static class NullConfigurationOwner extends MockConfigurationOwner {
        public ConfigurationSession provideConfigurationSession() {
            return null;
        }

        @Override
        public void addOwnerStateListener(OwnerStateListener listener) {
        }

        @Override
        public void removeOwnerStateListener(OwnerStateListener listener) {
        }

        @Override
        public ArooaElement rootElement() {
            return new ArooaElement("test");
        }

        @Override
        public SerializableDesignFactory rootDesignFactory() {
            return new OurDesignFactory();
        }
    }

    @Test
    public void testNullConfiguration() {

        ComponentOwnerHandlerFactory test = new ComponentOwnerHandlerFactory();

        ServerInterfaceHandler serverHandler = test.createServerHandler(
                new NullConfigurationOwner(), new OurServerSideToolkit());

        OurClientToolkit clientToolkit = new OurClientToolkit();
        clientToolkit.handler = serverHandler;

        ConfigurationOwner clientHandler =
                new ComponentOwnerHandlerFactory.ClientFactory(
                ).createClientHandler(new MockConfigurationOwner(), clientToolkit);

        ConfigurationSession configurationSession = clientHandler.provideConfigurationSession();

        assertNull(configurationSession);
    }

    private static class NullDropPointOwner extends MockConfigurationOwner {
        public ConfigurationSession provideConfigurationSession() {
            return new MockConfigurationSession() {
                public DragPoint dragPointFor(Object component) {
                    return null;
                }

                @Override
                public void addSessionStateListener(
                        SessionStateListener listener) {
                }

                @Override
                public void removeSessionStateListener(
                        SessionStateListener listener) {
                }
            };
        }

        @Override
        public void addOwnerStateListener(OwnerStateListener listener) {
        }

        @Override
        public void removeOwnerStateListener(OwnerStateListener listener) {
        }

        @Override
        public ArooaElement rootElement() {
            return new ArooaElement("test");
        }

        @Override
        public SerializableDesignFactory rootDesignFactory() {
            return new OurDesignFactory();
        }
    }

    @Test
    public void testNullDropPointConfiguration() {

        ComponentOwnerHandlerFactory test = new ComponentOwnerHandlerFactory();

        ServerInterfaceHandler serverHandler = test.createServerHandler(
                new NullDropPointOwner(), new OurServerSideToolkit());

        OurClientToolkit clientToolkit = new OurClientToolkit();
        clientToolkit.handler = serverHandler;

        ConfigurationOwner clientHandler =
                new ComponentOwnerHandlerFactory.ClientFactory(
                ).createClientHandler(new MockConfigurationOwner(), clientToolkit);

        ConfigurationSession configurationSession = clientHandler.provideConfigurationSession();

        assertNotNull(configurationSession);

        DragPoint dragPoint = configurationSession.dragPointFor(clientHandler);

        assertNull(dragPoint);
    }

    /////////////////////
    // Modified Stuff

    private static class ModifiedNotifySession extends MockConfigurationSession {
        SessionStateListener listener;

        @Override
        public void addSessionStateListener(SessionStateListener listener) {
            assertNull(this.listener);
            this.listener = listener;
        }

        @Override
        public void removeSessionStateListener(SessionStateListener listener) {
            assertEquals(this.listener, listener);
            this.listener = null;
        }

        void modified() {
            this.listener.sessionModified(new ConfigSessionEvent(this));
        }

        void saved() {
            this.listener.sessionSaved(new ConfigSessionEvent(this));
        }
    }


    private static class ModifiedOwner extends MockConfigurationOwner {

        final ModifiedNotifySession session = new ModifiedNotifySession();

        public ConfigurationSession provideConfigurationSession() {
            return session;
        }

        @Override
        public void addOwnerStateListener(OwnerStateListener listener) {
        }

        @Override
        public void removeOwnerStateListener(OwnerStateListener listener) {
        }

        @Override
        public ArooaElement rootElement() {
            return new ArooaElement("test");
        }

        @Override
        public SerializableDesignFactory rootDesignFactory() {
            return new OurDesignFactory();
        }
    }

    private static class SessionResultListener implements SessionStateListener {

        ConfigSessionEvent event;

        boolean modified;

        public void sessionModified(ConfigSessionEvent event) {
            this.event = event;
            modified = true;
        }

        public void sessionSaved(ConfigSessionEvent event) {
            this.event = event;
            modified = false;
        }
    }

    private static class ModifiedClientToolkit extends OurClientToolkit {

        ModifiedServerSideToolkit serverToolkit;

        @Override
        public <T> void registerNotificationListener(NotificationType<T> eventType,
                                                     NotificationListener<T> notificationListener) {
            assertEquals(ComponentOwnerHandlerFactory.MODIFIED_NOTIF_TYPE, eventType);
            assertNull(serverToolkit.listener);
            serverToolkit.listener = notificationListener;
        }

        @Override
        public <T> void removeNotificationListener(NotificationType<T> eventType,
                                                   NotificationListener<T> notificationListener) {
            assertEquals(serverToolkit.listener, notificationListener);
            serverToolkit.listener = null;
        }
    }

    private static class ModifiedServerSideToolkit extends MockServerSideToolkit {

        NotificationListener<?> listener;

        @Override
        public <T> Notification<T> createNotification(NotificationType<T> type, T userData) {
            assertEquals(ComponentOwnerHandlerFactory.MODIFIED_NOTIF_TYPE, type);
            return new Notification<>(1L, type, 0, userData);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public void sendNotification(Notification<?> notification) {
            if (listener != null) {
                ((NotificationListener) listener).handleNotification(notification);
            }
        }

        @Override
        public void runSynchronized(Runnable runnable) {
            runnable.run();
        }
    }

    @Test
    public void testSessionStateNotification() {

        ComponentOwnerHandlerFactory test = new ComponentOwnerHandlerFactory();

        ModifiedOwner owner = new ModifiedOwner();

        ModifiedServerSideToolkit serverToolkit = new ModifiedServerSideToolkit();
        ServerInterfaceHandler serverHandler = test.createServerHandler(
                owner, serverToolkit);

        ModifiedClientToolkit clientToolkit = new ModifiedClientToolkit();
        clientToolkit.handler = serverHandler;
        clientToolkit.serverToolkit = serverToolkit;

        ConfigurationOwner clientHandler =
                new ComponentOwnerHandlerFactory.ClientFactory(
                ).createClientHandler(new MockConfigurationOwner(), clientToolkit);

        SessionResultListener results = new SessionResultListener();

        clientHandler.provideConfigurationSession(
        ).addSessionStateListener(results);

        assertFalse(results.modified);
        assertNull(results.event);

        owner.session.modified();

        assertTrue(results.modified);
        assertNotNull(results.event);

        owner.session.saved();

        assertFalse(results.modified);
        assertNotNull(results.event);

        clientHandler.provideConfigurationSession(
        ).removeSessionStateListener(results);

        owner.session.modified();
        assertNotNull(results.event);

        assertFalse(results.modified);
    }


    private static class NotifyingOwner extends MockConfigurationOwner {

        OwnerStateListener listener;

        ConfigurationSession session;

        public ConfigurationSession provideConfigurationSession() {
            return session;
        }

        public void setSession(ConfigurationSession session) {
            assertNotNull(session);
            this.session = session;
            this.listener.sessionChanged(new ConfigOwnerEvent(this,
                    ConfigOwnerEvent.Change.SESSION_CREATED));
        }

        @Override
        public void addOwnerStateListener(OwnerStateListener listener) {
            assertNull(this.listener);
            this.listener = listener;
        }

        @Override
        public void removeOwnerStateListener(OwnerStateListener listener) {
            assertEquals(this.listener, listener);
            this.listener = null;
        }

        @Override
        public ArooaElement rootElement() {
            return new ArooaElement("test");
        }

        @Override
        public SerializableDesignFactory rootDesignFactory() {
            return new OurDesignFactory();
        }
    }

    private static class ResultListener implements OwnerStateListener {

        ConfigOwnerEvent event;

        int count;

        public void sessionChanged(ConfigOwnerEvent event) {
            this.event = event;
            ++count;
        }
    }

    private static class NotifyClientToolkit extends OurClientToolkit {

        NotifyServerSideToolkit serverToolkit;

        @Override
        public <T> void registerNotificationListener(NotificationType<T> eventType,
                                                     NotificationListener<T> notificationListener) {
            assertEquals(ComponentOwnerHandlerFactory.CHANGE_NOTIF_TYPE, eventType);
            assertNull(serverToolkit.listener);
            serverToolkit.listener = notificationListener;
        }

        @Override
        public <T> void removeNotificationListener(NotificationType<T> eventType,
                                                   NotificationListener<T> notificationListener) {
            assertEquals(serverToolkit.listener, notificationListener);
            serverToolkit.listener = null;
        }
    }

    private static class NotifyServerSideToolkit extends MockServerSideToolkit {

        NotificationListener<?> listener;

        @Override
        public <T> Notification<T> createNotification(NotificationType<T> type, T userData) {
            assertEquals(ComponentOwnerHandlerFactory.CHANGE_NOTIF_TYPE, type);
            return new Notification<>(1L, type, 0, userData);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public void sendNotification(Notification<?> notification) {
            if (listener != null) {
                ((NotificationListener) listener).handleNotification(notification);
            }
        }

        @Override
        public void runSynchronized(Runnable runnable) {
            runnable.run();
        }
    }

    @Test
    public void testSessionChangeNotification() {

        ComponentOwnerHandlerFactory test = new ComponentOwnerHandlerFactory();

        NotifyingOwner owner = new NotifyingOwner();

        NotifyServerSideToolkit serverToolkit = new NotifyServerSideToolkit();
        ServerInterfaceHandler serverHandler = test.createServerHandler(
                owner, serverToolkit);

        NotifyClientToolkit clientToolkit = new NotifyClientToolkit();
        clientToolkit.handler = serverHandler;
        clientToolkit.serverToolkit = serverToolkit;

        ConfigurationOwner clientProxy = new MockConfigurationOwner();

        ConfigurationOwner clientHandler =
                new ComponentOwnerHandlerFactory.ClientFactory(
                ).createClientHandler(clientProxy, clientToolkit);

        ResultListener results = new ResultListener();

        clientHandler.addOwnerStateListener(results);

        assertEquals(0, results.count);

        ConfigurationSession clientSession = clientHandler.provideConfigurationSession();

        assertNull(clientSession);

        owner.setSession(new ModifiedNotifySession());

        assertEquals(clientProxy, results.event.getSource());

        assertEquals(1, results.count);

        clientSession = clientHandler.provideConfigurationSession();

        assertNotNull(clientSession);

        clientHandler.removeOwnerStateListener(results);

        owner.setSession(new ModifiedNotifySession());

        assertEquals(1, results.count);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testPossibleChildren() throws Throwable {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("TEST", "<oddjob/>"));
        oddjob.load();

        ServerSideToolkit serverToolkit = mock(ServerSideToolkit.class);
        ServerInterfaceHandler serverHandler = new ComponentOwnerHandlerFactory()
                .createServerHandler(oddjob, serverToolkit);

        ClientInterfaceHandlerFactory<ConfigurationOwner> factory
                = new ComponentOwnerHandlerFactory.ClientFactory();

        ConfigurationOwner proxy = mock(ConfigurationOwner.class);
        ClientSideToolkit clientToolkit = mock(ClientSideToolkit.class);

        doAnswer(invocation -> {
            if (invocation.getArguments().length == 1) {
                return serverHandler.invoke(
                        invocation.getArgument(0), new Object[0]);
            } else if (invocation.getArguments().length == 2) {
                return serverHandler.invoke(
                        invocation.getArgument(0), new Object[]{invocation.getArgument(1)});
            } else {
                throw new RuntimeException("How do we handle VarArgs?");
            }
        })
                .when(clientToolkit)
                .invoke(Mockito.any(RemoteOperation.class), ArgumentMatchers.any());
        Map<NotificationType<?>, NotificationListener<?>> listenerMap = new HashMap<>();
        doAnswer(invocation -> {
            listenerMap.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(clientToolkit).
                registerNotificationListener(any(NotificationType.class), any(NotificationListener.class));
        AtomicInteger sequence = new AtomicInteger();
        doAnswer(invocation -> new Notification(-1,
                invocation.getArgument(0),
                sequence.getAndIncrement(),
                invocation.getArgument(1)))
                .when(serverToolkit)
                .createNotification(any(NotificationType.class), any());
        doAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            listenerMap.get(n.getType()).handleNotification(n);
            return null;
        })
                .when(serverToolkit)
                .sendNotification(any(Notification.class));

        ConfigurationOwner configurationOwner = factory.createClientHandler(proxy, clientToolkit);

        DragPoint dragPoint = configurationOwner.provideConfigurationSession().dragPointFor(oddjob);

        QTag[] result = dragPoint.possibleChildren();

        logger.info(Arrays.toString(result));

        MatcherAssert.assertThat(result, hasItemInArray(new QTag("bean")));
        MatcherAssert.assertThat(result, hasItemInArray(new QTag("echo")));
    }
}
