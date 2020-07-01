package org.oddjob.jmx.handlers;

import org.junit.Assert;
import org.junit.Test;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.life.ClassLoaderClassResolver;
import org.oddjob.arooa.parsing.*;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.xmlunit.matchers.CompareMatcher;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ComponentOwnerHandlerFactoryTest {

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

            return new DragPoint() {

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

                public boolean supportsCut() {
                    return true;
                }

                public boolean supportsPaste() {
                    return true;
                }

                public String copy() {
                    return "apples";
                }

                public void cut() {
                    cut = true;
                }

                public ConfigurationHandle parse(ArooaContext parentContext) {

                    throw new RuntimeException("Unexpected.");
                }

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

        MySessionLite sess = new MySessionLite();

        public ConfigurationSession provideConfigurationSession() {
            return sess;
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

        ClientHandlerResolver<ConfigurationOwner> clientResolver =
                test.clientHandlerFactory();

        ClientInterfaceHandlerFactory<ConfigurationOwner> cihf =
                clientResolver.resolve(new ClassLoaderClassResolver(
                        getClass().getClassLoader()));

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
                new ComponentOwnerHandlerFactory.ClientConfigurationOwnerHandlerFactory(
                ).createClientHandler(new MockConfigurationOwner(), clientToolkit);

        Object ourComponent = new Object();

        DragPoint local = clientHandler.provideConfigurationSession().dragPointFor(
                ourComponent);

        assertTrue(local.supportsCut());
        assertTrue(local.supportsPaste());

        assertSame(ourComponent, compO.sess.component);

        DragTransaction trn = local.beginChange(ChangeHow.FRESH);
        local.cut();
        trn.commit();

        assertTrue(compO.sess.committed);
        assertTrue(compO.sess.cut);

        assertEquals("apples", local.copy());

        local.paste(2, "oranges");

        assertEquals(2, compO.sess.pasteIndex);
        assertEquals("oranges", compO.sess.pasteText);

        clientHandler.provideConfigurationSession().save();
        assertTrue(compO.sess.saved);

    }


    private static class OurComponentOwner2 extends MockConfigurationOwner {

        DragPoint drag;
        ConfigurationHandle handle;

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

        final ConfigurationHandle handle = parser.parse(config);

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
                new ComponentOwnerHandlerFactory.ClientConfigurationOwnerHandlerFactory(
                ).createClientHandler(new MockConfigurationOwner(), clientToolkit);

        DragPoint local = clientHandler.provideConfigurationSession().dragPointFor(root);

        XMLArooaParser parser2 = new XMLArooaParser();

        ConfigurationHandle handle2 = parser2.parse(local);

        ArooaContext context = handle2.getDocumentContext();

        XMLConfiguration replacement = new XMLConfiguration("TEST",
                "<class id='oranges'/>");

        CutAndPasteSupport.replace(context.getParent(),
                context, replacement);
        handle2.save();

        clientHandler.provideConfigurationSession().save();

        String expected = "<class id=\"oranges\"/>" +
                System.getProperty("line.separator");

        Assert.assertThat(savedXML.get(), CompareMatcher.isSimilarTo(expected));
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
                new ComponentOwnerHandlerFactory.ClientConfigurationOwnerHandlerFactory(
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
                new ComponentOwnerHandlerFactory.ClientConfigurationOwnerHandlerFactory(
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
        public void registerNotificationListener(String eventType,
                                                 NotificationListener notificationListener) {
            assertEquals(ComponentOwnerHandlerFactory.MODIFIED_NOTIF_TYPE, eventType);
            assertNull(serverToolkit.listener);
            serverToolkit.listener = notificationListener;
        }

        @Override
        public void removeNotificationListener(String eventType,
                                               NotificationListener notificationListener) {
            assertEquals(serverToolkit.listener, notificationListener);
            serverToolkit.listener = null;
        }
    }

    private static class ModifiedServerSideToolkit extends MockServerSideToolkit {

        NotificationListener listener;

        @Override
        public Notification createNotification(String type, Object userData) {
            assertEquals(ComponentOwnerHandlerFactory.MODIFIED_NOTIF_TYPE, type);
            return new Notification(1L, type, 0, userData);
        }

        @Override
        public void sendNotification(Notification notification) {
            if (listener != null) {
                listener.handleNotification(notification);
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
                new ComponentOwnerHandlerFactory.ClientConfigurationOwnerHandlerFactory(
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
        public void registerNotificationListener(String eventType,
                                                 NotificationListener notificationListener) {
            assertEquals(ComponentOwnerHandlerFactory.CHANGE_NOTIF_TYPE, eventType);
            assertNull(serverToolkit.listener);
            serverToolkit.listener = notificationListener;
        }

        @Override
        public void removeNotificationListener(String eventType,
                                               NotificationListener notificationListener) {
            assertEquals(serverToolkit.listener, notificationListener);
            serverToolkit.listener = null;
        }
    }

    private static class NotifyServerSideToolkit extends MockServerSideToolkit {

        NotificationListener listener;

        @Override
        public Notification createNotification(String type, Object userData) {
            assertEquals(ComponentOwnerHandlerFactory.CHANGE_NOTIF_TYPE, type);
            return new Notification(1L, type, 0, userData);
        }

        @Override
        public void sendNotification(Notification notification) {
            if (listener != null) {
                listener.handleNotification(notification);
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
                new ComponentOwnerHandlerFactory.ClientConfigurationOwnerHandlerFactory(
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
}
