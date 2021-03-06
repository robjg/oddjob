package org.oddjob.jmx.handlers;

import org.oddjob.arooa.*;
import org.oddjob.arooa.forms.FormsLookup;
import org.oddjob.arooa.forms.FormsLookupFromDescriptor;
import org.oddjob.arooa.json.JsonArooaParser;
import org.oddjob.arooa.json.JsonArooaParserBuilder;
import org.oddjob.arooa.json.JsonConfiguration;
import org.oddjob.arooa.parsing.*;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This should be ConfigurationOwnerHandlerFactory.
 *
 * @author rob
 */
public class ComponentOwnerHandlerFactory
        implements ServerInterfaceHandlerFactory<ConfigurationOwner, ConfigurationOwner> {

    public static final HandlerVersion VERSION = new HandlerVersion(5, 0);

    public static final NotificationType<Boolean> MODIFIED_NOTIF_TYPE =
            NotificationType.ofName("oddjob.config.modified")
                    .andDataType(Boolean.class);

    public static final NotificationType<ConfigOwnerEvent.Change> CHANGE_NOTIF_TYPE =
            NotificationType.ofName("oddjob.config.changed")
                    .andDataType(ConfigOwnerEvent.Change.class);

    private static final JMXOperationPlus<Integer> SESSION_AVAILABLE =
            new JMXOperationPlus<>(
                    "ConfigurationOwner.sessionAvailable",
                    "",
                    Integer.class,
                    MBeanOperationInfo.INFO
            );

    private static final JMXOperationPlus<DragPointInfo> DRAG_POINT_INFO =
            new JMXOperationPlus<>(
                    "dragPointInfo",
                    "",
                    DragPointInfo.class,
                    MBeanOperationInfo.INFO
            ).addParam("component", Object.class, "The Component");

    private static final JMXOperationPlus<String> CUT =
            new JMXOperationPlus<>(
                    "configCut",
                    "",
                    String.class,
                    MBeanOperationInfo.ACTION_INFO
            ).addParam("component", Object.class, "The Component");

    private static final JMXOperationPlus<String> COPY =
            new JMXOperationPlus<>(
                    "configCopy",
                    "",
                    String.class,
                    MBeanOperationInfo.ACTION_INFO
            ).addParam("component", Object.class, "The Component");

    private static final JMXOperationPlus<Void> DELETE =
            new JMXOperationPlus<>(
                    "configDelete",
                    "",
                    Void.TYPE,
                    MBeanOperationInfo.ACTION_INFO
            ).addParam("component", Object.class, "The Component");

    private static final JMXOperationPlus<String> PASTE =
            new JMXOperationPlus<>(
                    "configPaste",
                    "",
                    String.class,
                    MBeanOperationInfo.ACTION
            ).addParam("component", Object.class, "The Component")
                    .addParam("index", Integer.TYPE, "The Index")
                    .addParam("config", String.class, "The XML Configuration");


    private static final JMXOperationPlus<PossibleChildren> POSSIBLE_CHILDREN =
            new JMXOperationPlus<>(
                    "possibleChildren",
                    "Returns the possible tags that can be used in an add job function",
                    PossibleChildren.class,
                    MBeanOperationInfo.ACTION_INFO
            ).addParam("component", Object.class, "The Component");

    private static final JMXOperationPlus<Boolean> IS_MODIFIED =
            new JMXOperationPlus<>(
                    "configIsModified",
                    "",
                    Boolean.class,
                    MBeanOperationInfo.INFO);

    private static final JMXOperationPlus<String> SAVE =
            new JMXOperationPlus<>(
                    "configSave",
                    "",
                    String.class,
                    MBeanOperationInfo.ACTION);

    private static final JMXOperationPlus<Void> REPLACE =
            new JMXOperationPlus<>(
                    "configReplace",
                    "",
                    Void.TYPE,
                    MBeanOperationInfo.INFO
            ).addParam("component", Object.class, "");

    private static final JMXOperationPlus<ComponentOwnerInfo> INFO =
            new JMXOperationPlus<>(
                    "componentOwnerInfo",
                    "Basic Info For Component Owner",
                    ComponentOwnerInfo.class,
                    MBeanOperationInfo.INFO
            );

    private static final JMXOperationPlus<String> FORM_FOR =
            new JMXOperationPlus<>(
                    "formFor",
                    "",
                    String.class,
                    MBeanOperationInfo.INFO
            ).addParam("component", Object.class, "");

    private static final JMXOperationPlus<String> BLANK_FORM =
            new JMXOperationPlus<>(
                    "blankForm",
                    "",
                    String.class,
                    MBeanOperationInfo.INFO
            ).addParam("isComponent", boolean.class, "")
                    .addParam("element", String.class, "")
                    .addParam("propertyClass", String.class, "");

    private static final JMXOperationPlus<Void> REPLACE_JSON =
            new JMXOperationPlus<>(
                    "configReplaceJson",
                    "Replace existing configuration with new configuration in JSON format",
                    Void.TYPE,
                    MBeanOperationInfo.INFO
            ).addParam("component", Object.class, "The component who's configuration is being replaced")
                    .addParam("configuration", String.class, "The new configuration in JSON");

    /*
     * (non-Javadoc)
     * @see org.oddjob.jmx.server.ServerInterfaceHandlerFactory#interfaceClass()
     */
    @Override
    public Class<ConfigurationOwner> serverClass() {
        return ConfigurationOwner.class;
    }

    @Override
    public Class<ConfigurationOwner> clientClass() {
        return ConfigurationOwner.class;
    }

    @Override
    public HandlerVersion getHandlerVersion() {
        return VERSION;
    }

    @Override
    public MBeanAttributeInfo[] getMBeanAttributeInfo() {
        return new MBeanAttributeInfo[0];
    }

    @Override
    public MBeanOperationInfo[] getMBeanOperationInfo() {
        return new MBeanOperationInfo[]{
                INFO.getOpInfo(),
                SESSION_AVAILABLE.getOpInfo(),
                DRAG_POINT_INFO.getOpInfo(),
                CUT.getOpInfo(),
                COPY.getOpInfo(),
                PASTE.getOpInfo(),
                DELETE.getOpInfo(),
                POSSIBLE_CHILDREN.getOpInfo(),
                SAVE.getOpInfo(),
                IS_MODIFIED.getOpInfo(),
                REPLACE.getOpInfo(),
                FORM_FOR.getOpInfo(),
                BLANK_FORM.getOpInfo(),
                REPLACE_JSON.getOpInfo()
        };
    }

    @Override
    public List<NotificationType<?>> getNotificationTypes() {
        return Arrays.asList(MODIFIED_NOTIF_TYPE);
    }

    @Override
    public ServerInterfaceHandler createServerHandler(
            ConfigurationOwner target, ServerSideToolkit ojmb) {
        return new ServerComponentOwnerHandler(target, ojmb);
    }

    public static class ClientFactory
            implements ClientInterfaceHandlerFactory<ConfigurationOwner> {

        @Override
        public Class<ConfigurationOwner> interfaceClass() {
            return ConfigurationOwner.class;
        }

        @Override
        public HandlerVersion getVersion() {
            return VERSION;
        }

        @Override
        public ConfigurationOwner createClientHandler(ConfigurationOwner proxy, ClientSideToolkit toolkit) {
            return new ClientComponentOwnerHandler(proxy, toolkit);
        }
    }

    /**
     * The Client {@link ConfigurationOwner}
     */
    static class ClientComponentOwnerHandler implements ConfigurationOwner {

        private final ClientSideToolkit clientToolkit;

        private final ConfigurationOwnerSupport ownerSupport;

        private final SerializableDesignFactory rootDesignFactory;

        private final ArooaElement rootElement;

        private volatile boolean listening;

        private final NotificationListener<ConfigOwnerEvent.Change> listener =
                notification -> updateSession(notification.getData());

        ClientComponentOwnerHandler(ConfigurationOwner proxy, final ClientSideToolkit toolkit) {
            this.clientToolkit = toolkit;

            ownerSupport = new ConfigurationOwnerSupport(proxy);
            updateSession(null);

            ownerSupport.setOnFirst(() -> {
                updateSession(null);
                toolkit.registerNotificationListener(
                        CHANGE_NOTIF_TYPE, listener);
                listening = true;
            });

            ownerSupport.setOnEmpty(() -> {
                listening = false;
                toolkit.removeNotificationListener(
                        CHANGE_NOTIF_TYPE, listener);
            });

            try {
                ComponentOwnerInfo info = clientToolkit.invoke(
                        INFO);

                rootDesignFactory = info.rootDesignFactory;
                rootElement = info.rootElement;
            } catch (Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        @Override
        public ConfigurationSession provideConfigurationSession() {
            if (!listening) {
                updateSession(null);
            }
            return ownerSupport.provideConfigurationSession();
        }

        /**
         * Lots of complicated logic to see if the server
         * configuration session has changed.
         *
         * @param change
         */
        private void updateSession(ConfigOwnerEvent.Change change) {
            if (change == null ||
                    change == ConfigOwnerEvent.Change.SESSION_CREATED) {

                Integer newId;
                try {
                    newId = clientToolkit.invoke(SESSION_AVAILABLE);
                } catch (Throwable e) {
                    // need to rethink this. Fails when client has been stopped.
                    // but why is it being being called at all?
                    newId = null;
                }

                if (newId == null) {
                    ownerSupport.setConfigurationSession(null);
                } else {
                    ClientConfigurationSessionHandler existing =
                            (ClientConfigurationSessionHandler)
                                    ownerSupport.provideConfigurationSession();

                    if (existing == null || existing.id != newId) {
                        ownerSupport.setConfigurationSession(null);
                        ownerSupport.setConfigurationSession(
                                new ClientConfigurationSessionHandler(
                                        clientToolkit, newId));
                    }
                }
            } else {
                ownerSupport.setConfigurationSession(null);
            }
        }

        @Override
        public void addOwnerStateListener(OwnerStateListener listener) {
            ownerSupport.addOwnerStateListener(listener);
        }

        @Override
        public void removeOwnerStateListener(OwnerStateListener listener) {
            ownerSupport.removeOwnerStateListener(listener);
        }

        @Override
        public SerializableDesignFactory rootDesignFactory() {
            return rootDesignFactory;
        }

        @Override
        public ArooaElement rootElement() {
            return rootElement;
        }
    }

    /**
     * The client {@link ConfigurationSession}.
     */
    static class ClientConfigurationSessionHandler
            implements ConfigurationSession {

        private final ClientSideToolkit clientToolkit;

        private final ConfigurationSessionSupport sessionSupport;

        private final int id;

        /**
         * Cache the last one because menu asks for it several times in a row
         */
        private volatile ClientDragPoint lastDragPoint;

        private final NotificationListener<Boolean> listener =
                new NotificationListener<Boolean>() {
                    @Override
                    public void handleNotification(Notification<Boolean> notification) {
                        Boolean modified = notification.getData();
                        if (modified) {
                            sessionSupport.modified();
                        } else {
                            sessionSupport.saved();
                        }
                    }
                };

        public ClientConfigurationSessionHandler(final ClientSideToolkit clientToolkit,
                                                 int id) {

            this.id = id;
            this.clientToolkit = clientToolkit;
            sessionSupport = new ConfigurationSessionSupport(this);
            sessionSupport.setOnFirst(() -> clientToolkit.registerNotificationListener(MODIFIED_NOTIF_TYPE,
                    listener));
            sessionSupport.setOnEmpty(() -> clientToolkit.removeNotificationListener(MODIFIED_NOTIF_TYPE,
                    listener));
        }

        class ClientDragPoint implements DragPoint {

            private final Object component;

            private final DragPointInfo dragPointInfo;

            ClientDragPoint(Object component, DragPointInfo dragPointInfo) {
                this.component = component;
                this.dragPointInfo = dragPointInfo;
            }


            @Override
            public boolean supportsCut() {
                return dragPointInfo.supportsCut;
            }

            @Override
            public boolean supportsPaste() {
                return dragPointInfo.supportsPaste;
            }

            @Override
            public DragTransaction beginChange(ChangeHow how) {
                // Only create a fake client DragTransaction. The server will
                // create a real one.
                return new DragTransaction() {

                    @Override
                    public void rollback() {
                    }

                    @Override
                    public void commit() {
                    }
                };
            }

            @Override
            public String copy() {
                try {
                    return clientToolkit.invoke(
                            COPY, component);
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }

            @Override
            public String cut() {
                try {
                    return clientToolkit.invoke(
                            CUT, component);
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }

            @Override
            public void delete() {
                try {
                    clientToolkit.invoke(
                            DELETE, component);
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }

            @Override
            public <P extends ParseContext<P>> ConfigurationHandle<P> parse(
                    P parentContext) {
                try {
                    String configAsXml = copy();

                    final XMLConfiguration config =
                            new XMLConfiguration("Server Config",
                                    configAsXml);

                    final ConfigurationHandle<P> handle =
                            config.parse(parentContext);

                    return new ConfigurationHandle<P>() {
                        @Override
                        public P getDocumentContext() {
                            return handle.getDocumentContext();
                        }

                        @Override
                        public void save()
                                throws ArooaParseException {

                            config.setSaveHandler(xml -> {
                                try {
                                    if (xml.equals(configAsXml)) {
                                        return;
                                    }

                                    clientToolkit.invoke(
                                            REPLACE, component, xml);
                                } catch (Throwable e) {
                                    throw new UndeclaredThrowableException(e);
                                }
                            });

                            handle.save();
                        }
                    };

                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }

            @Override
            public void paste(int index, String config) {
                try {
                    clientToolkit.invoke(
                            PASTE,
                            component,
                            index,
                            config);
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }

            @Override
            public QTag[] possibleChildren() {
                if (!dragPointInfo.supportsPaste) {
                    throw new IllegalArgumentException("Component doesn't support children. Check supportsPaste");
                }

                PossibleChildren possibleChildren;
                try {
                    possibleChildren = clientToolkit.invoke(
                            POSSIBLE_CHILDREN,
                            component);
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }

                PrefixMapping prefixMapping = tag -> {
                    try {
                        return new URI(possibleChildren.prefixMapping.get(tag));
                    } catch (URISyntaxException e) {
                        throw new UndeclaredThrowableException(e);
                    }
                };

                return Arrays.stream(possibleChildren.tags)
                        .map(prefixMapping::qTagFor)
                        .toArray(QTag[]::new);
            }
        }

        @Override
        public DragPoint dragPointFor(Object component) {

            if (component == null) {
                throw new NullPointerException("No component.");
            }

            ClientDragPoint lastDragPoint = this.lastDragPoint;
            if (lastDragPoint != null && lastDragPoint.component == component) {
                return lastDragPoint;
            }

            try {
                final DragPointInfo dragPointInfo =
                        clientToolkit.invoke(
                                DRAG_POINT_INFO, component);

                ClientDragPoint dragPoint;
                if (dragPointInfo == null) {
                    dragPoint = null;
                } else {
                    dragPoint = new ClientDragPoint(component, dragPointInfo);
                }

                this.lastDragPoint = dragPoint;
                return dragPoint;

            } catch (Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        @Override
        public void save() {
            try {
                clientToolkit.invoke(
                        SAVE);
            } catch (Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        @Override
        public boolean isModified() {
            try {
                return clientToolkit.invoke(
                        IS_MODIFIED);
            } catch (Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        @Override
        public void addSessionStateListener(SessionStateListener listener) {
            sessionSupport.addSessionStateListener(listener);
        }

        @Override
        public void removeSessionStateListener(SessionStateListener listener) {
            sessionSupport.removeSessionStateListener(listener);
        }

        @Override
        public ArooaDescriptor getArooaDescriptor() {
            return clientToolkit.getClientSession().getArooaSession().getArooaDescriptor();
        }


    }

    static class ServerComponentOwnerHandler implements ServerInterfaceHandler {

        private final ConfigurationOwner configurationOwner;

        private final ServerSideToolkit toolkit;

        private ConfigurationSession configurationSession;

        private final SessionStateListener modifiedListener = new SessionStateListener() {

            @Override
            public void sessionModified(ConfigSessionEvent event) {
                send(true);
            }

            @Override
            public void sessionSaved(ConfigSessionEvent event) {
                send(false);
            }

            void send(final boolean modified) {
                toolkit.runSynchronized(() -> {
                    Notification<Boolean> notification =
                            toolkit.createNotification(MODIFIED_NOTIF_TYPE, modified);
                    toolkit.sendNotification(notification);
                });
            }
        };

        private final OwnerStateListener configurationListener
                = new OwnerStateListener() {

            @Override
            public void sessionChanged(final ConfigOwnerEvent event) {
                configurationSession = configurationOwner.provideConfigurationSession();
                if (configurationSession != null) {
                    configurationSession.addSessionStateListener(modifiedListener);
                }

                toolkit.runSynchronized(() -> {
                    Notification<ConfigOwnerEvent.Change> notification =
                            toolkit.createNotification(CHANGE_NOTIF_TYPE, event.getChange());
                    toolkit.sendNotification(notification);
                });
            }
        };

        ServerComponentOwnerHandler(ConfigurationOwner configurationOwner, ServerSideToolkit serverToolkit) {
            this.configurationOwner = configurationOwner;
            this.toolkit = serverToolkit;
            configurationOwner.addOwnerStateListener(configurationListener);
            configurationSession = configurationOwner.provideConfigurationSession();
            if (configurationSession != null) {
                configurationSession.addSessionStateListener(modifiedListener);
            }
        }

        @Override
        public Object invoke(RemoteOperation<?> operation, Object[] params) throws MBeanException, ReflectionException {

            if (INFO.equals(operation)) {

                return new ComponentOwnerInfo(configurationOwner);
            }

            if (SESSION_AVAILABLE.equals(operation)) {
                if (configurationSession == null) {
                    return null;
                } else {
                    return System.identityHashCode(configurationSession);
                }
            }

            if (configurationSession == null) {
                throw new MBeanException(new IllegalStateException("No Config Session - Method " +
                        operation + " should not have been called!"));
            }

            if (SAVE.equals(operation)) {

                try {
                    configurationSession.save();
                    return null;
                } catch (ArooaParseException e) {
                    throw new MBeanException(e);
                }
            }

            if (IS_MODIFIED.equals(operation)) {

                return configurationSession.isModified();
            }

            if (BLANK_FORM.equals(operation)) {

                boolean isComponent = (Boolean) params[0];
                String element = (String) params[1];
                String propertyClass = (String) params[2];

                FormsLookup formsLookup = new FormsLookupFromDescriptor(
                        configurationSession.getArooaDescriptor());

                ArooaConfiguration configuration = formsLookup.blankForm(
                        isComponent ? ArooaType.COMPONENT : ArooaType.VALUE,
                        element,
                        propertyClass);

                StringWriter json = new StringWriter();
                JsonArooaParser parser = new JsonArooaParserBuilder()
                        .withNamespaceMappings(FormsLookup.formsNamespaces())
                        .withWriter(json)
                        .build();
                try {
                    parser.parse(configuration);
                } catch (ArooaParseException e) {
                    throw new MBeanException(e);
                }

                return json.toString();
            }

            // Operations below here require a drag point.

            DragPoint dragPoint = null;
            Object component = null;
            if (params != null && params.length > 0) {

                component = params[0];
                dragPoint = configurationSession.dragPointFor(component);
            }

            if (DRAG_POINT_INFO.equals(operation)) {
                if (dragPoint == null) {
                    return null;
                } else {
                    return new DragPointInfo(dragPoint);
                }
            }

            if (dragPoint == null) {
                throw new MBeanException(new IllegalStateException("Null Drag Point for component [" +
                        component + "] - Method " +
                        operation + " should not have been called!"));
            }

            if (FORM_FOR.equals(operation)) {

                FormsLookup formsLookup = new FormsLookupFromDescriptor(
                        configurationSession.getArooaDescriptor());

                XMLConfiguration xmlConfiguration = new XMLConfiguration(
                        "XML Configuration for " + dragPoint.toString(),
                        dragPoint.copy());

                ArooaConfiguration configuration;
                try {
                    configuration = formsLookup.formFor(xmlConfiguration);
                } catch (ArooaParseException e) {
                    throw new MBeanException(e);
                }

                StringWriter json = new StringWriter();
                JsonArooaParser parser = new JsonArooaParserBuilder()
                        .withNamespaceMappings(FormsLookup.formsNamespaces())
                        .withWriter(json)
                        .build();
                try {
                    parser.parse(configuration);
                } catch (ArooaParseException e) {
                    throw new MBeanException(e);
                }

                return json.toString();
            }

            if (COPY.equals(operation)) {

                return dragPoint.copy();
            } else if (CUT.equals(operation)) {

                DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
                String copy = dragPoint.cut();
                try {
                    trn.commit();
                } catch (ArooaParseException e) {
                    trn.rollback();
                    throw new MBeanException(e);
                }

                return copy;
            } else if (DELETE.equals(operation)) {

                DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
                dragPoint.delete();
                try {
                    trn.commit();
                } catch (ArooaParseException e) {
                    trn.rollback();
                    throw new MBeanException(e);
                }

                return null;
            } else if (PASTE.equals(operation)) {

                Integer index = (Integer) params[1];
                String config = (String) params[2];

                DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
                try {
                    dragPoint.paste(index, config);
                    trn.commit();
                } catch (Exception e) {
                    trn.rollback();
                    throw new MBeanException(e);
                }
                return null;
            } else if (REPLACE.equals(operation)) {

                String config = (String) params[1];

                try {
                    XMLArooaParser parser = new XMLArooaParser(configurationSession.getArooaDescriptor());
                    ConfigurationHandle<SimpleParseContext> handle = parser.parse(dragPoint);

                    SimpleParseContext documentContext = handle.getDocumentContext();

                    CutAndPasteSupport.replace(documentContext.getParent(),
                            documentContext,
                            new XMLConfiguration("Edited Config", config));
                    handle.save();
                } catch (ArooaParseException e) {
                    throw new MBeanException(e);
                }
                return null;
            } else if (REPLACE_JSON.equals(operation)) {

                String config = (String) params[1];

                try {
                    XMLArooaParser parser = new XMLArooaParser(configurationSession.getArooaDescriptor());
                    ConfigurationHandle<SimpleParseContext> handle = parser.parse(dragPoint);

                    SimpleParseContext documentContext = handle.getDocumentContext();

                    CutAndPasteSupport.replace(documentContext.getParent(),
                            documentContext,
                            new JsonConfiguration(config)
                                    .withNamespaceMappings(configurationSession.getArooaDescriptor()));
                    handle.save();
                } catch (ArooaParseException e) {
                    throw new MBeanException(e);
                }
                return null;
            } else if (POSSIBLE_CHILDREN.equals(operation)) {

                Map<String, String> prefixMap = new HashMap<>();

                QTag[] qTags = dragPoint.possibleChildren();

                String[] tags = Arrays.stream(qTags)
                        .map(t -> {
                            Optional.ofNullable(t.getElement().getUri())
                                    .ifPresent(uri -> prefixMap.put(t.getPrefix(), uri.toString()));
                            return t.toString();
                        })
                        .toArray(String[]::new);

                return new PossibleChildren(prefixMap, tags);
            } else {
                throw new ReflectionException(
                        new IllegalStateException("Invoked for an unknown method [" +
                                operation.toString() + "]"),
                        operation.toString());
            }
        }

        @Override
        public void destroy() {
            configurationOwner.removeOwnerStateListener(configurationListener);
            if (configurationSession != null) {
                configurationSession.removeSessionStateListener(modifiedListener);
            }

        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}

class ComponentOwnerInfo implements Serializable {
    private static final long serialVersionUID = 2011090800L;

    final SerializableDesignFactory rootDesignFactory;

    final ArooaElement rootElement;

    ComponentOwnerInfo(ConfigurationOwner serverConfigOwner) {
        this.rootDesignFactory = serverConfigOwner.rootDesignFactory();
        this.rootElement = serverConfigOwner.rootElement();
    }
}

