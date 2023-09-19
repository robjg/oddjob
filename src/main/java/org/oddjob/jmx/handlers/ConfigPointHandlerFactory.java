package org.oddjob.jmx.handlers;

import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.parsing.*;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.Destroyable;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.remote.*;
import org.oddjob.remote.things.ConfigOperationInfo;
import org.oddjob.remote.things.ConfigOperationInfoFlags;
import org.oddjob.remote.things.ConfigPoint;
import org.oddjob.remote.util.NotifierListener;
import org.oddjob.remote.util.NotifierListenerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Remote Handler for an {@link ConfigPoint}. This is intended to replace DragPoint in {@link ComponentOwnerHandlerFactory}
 * however it's still a W.I.P.
 */
public class ConfigPointHandlerFactory
        implements ServerInterfaceHandlerFactory<Object, ConfigPoint> {

    private static final Logger logger = LoggerFactory.getLogger(ConfigPointHandlerFactory.class);

    public static final HandlerVersion VERSION = new HandlerVersion(1, 0);

    public static final NotificationType<Integer> CONFIG_POINT_INFO_NOTIFICATION_TYPE =
            NotificationType.ofName("org.oddjob.config")
                    .andDataType(int.class);


    private static final JMXOperationPlus<String> CUT =
            new JMXOperationPlus<>(
                    "configPointCut",
                    "Cut an Configuration Point",
                    String.class,
                    MBeanOperationInfo.ACTION_INFO
            );

    private static final JMXOperationPlus<String> COPY =
            new JMXOperationPlus<>(
                    "configPointCopy",
                    "Copy the Configuration for an Configuration Point",
                    String.class,
                    MBeanOperationInfo.ACTION_INFO
            );

    private static final JMXOperationPlus<Void> DELETE =
            new JMXOperationPlus<>(
                    "configPointDelete",
                    "Delete a Configuration Point",
                    Void.TYPE,
                    MBeanOperationInfo.ACTION
            );

    private static final JMXOperationPlus<Void> PASTE =
            new JMXOperationPlus<>(
                    "configPointPaste",
                    "Paste Configuration onto a Configuration Point",
                    Void.TYPE,
                    MBeanOperationInfo.ACTION
            ).addParam("index", Integer.TYPE, "The Index")
                    .addParam("config", String.class, "The XML or JSON Configuration");

    private static final JMXOperationPlus<PossibleChildren> POSSIBLE_CHILDREN =
            new JMXOperationPlus<>(
                    "configPointPossibleChildren",
                    "Returns the possible tags that can be used in an add job function",
                    PossibleChildren.class,
                    MBeanOperationInfo.ACTION_INFO
            );

    @Override
    public Class<Object> serverClass() {
        return Object.class;
    }

    @Override
    public Class<ConfigPoint> clientClass() {
        return ConfigPoint.class;
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
                CUT.getOpInfo(),
                COPY.getOpInfo(),
                DELETE.getOpInfo(),
                PASTE.getOpInfo(),
                POSSIBLE_CHILDREN.getOpInfo()
        };
    }

    @Override
    public List<NotificationType<?>> getNotificationTypes() {
        return Collections.singletonList(CONFIG_POINT_INFO_NOTIFICATION_TYPE);
    }

    @Override
    public ServerInterfaceHandler createServerHandler(Object target, ServerSideToolkit toolkit) {

        return toolkit.getContext().findAncestorOfType(ConfigurationOwner.class)
                .map(configurationOwner -> new ServerDragPointHandler(target, configurationOwner, toolkit))
                .orElse(null);

    }

    public static class ClientFactory
            implements ClientInterfaceHandlerFactory<ConfigPoint> {

        @Override
        public ConfigPoint createClientHandler(ConfigPoint ignored, ClientSideToolkit toolkit) {

            return new ClientDragPointHandler(toolkit);
        }

        @Override
        public HandlerVersion getVersion() {
            return VERSION;
        }

        @Override
        public Class<ConfigPoint> interfaceClass() {
            return ConfigPoint.class;
        }
    }

    /**
     * This needs to made thread safe.
     */
    static class ClientDragPointHandler implements ConfigPoint, NotificationListener<Integer>, Destroyable {

        private final ClientSideToolkit clientToolkit;

        private final List<Consumer<? super ConfigOperationInfo>> consumers = new LinkedList<>();

        private volatile ConfigOperationInfo configOperationInfo;

        ClientDragPointHandler(ClientSideToolkit clientToolkit) {
            this.clientToolkit = clientToolkit;
        }

        @Override
        public void addConfigurationSupportsConsumer(Consumer<? super ConfigOperationInfo> consumer) {

            if (consumers.isEmpty()) {
                consumers.add(consumer);
                try {
                    clientToolkit.registerNotificationListener(CONFIG_POINT_INFO_NOTIFICATION_TYPE, this);
                } catch (RemoteException e) {
                    throw new RemoteRuntimeException(e);
                }
            } else {
                Optional.ofNullable(configOperationInfo).ifPresent(consumer);
                consumers.add(consumer);
            }
        }

        @Override
        public void removeConfigurationSupportsConsumer(Consumer<? super ConfigOperationInfo> consumer) {

            consumers.remove(consumer);
            if (consumers.isEmpty()) {
                try {
                    clientToolkit.removeNotificationListener(CONFIG_POINT_INFO_NOTIFICATION_TYPE, this);
                } catch (RemoteException e) {
                    throw new RemoteRuntimeException(e);
                }
            }
        }

        @Override
        public void handleNotification(Notification<Integer> notification) {
            ConfigOperationInfo configOperationInfo = ConfigOperationInfoFlags.from(notification.getData());
            this.consumers.forEach(c -> c.accept(configOperationInfo));
            this.configOperationInfo = configOperationInfo;
        }

        @Override
        public String cut() {
            if (!Optional.ofNullable(configOperationInfo)
                    .map(ConfigOperationInfo::isCutSupported)
                    .orElse(false)) {
                throw new IllegalArgumentException("Component doesn't support cut.");
            }
            try {
                return clientToolkit.invoke(
                        CUT);
            } catch (Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        @Override
        public String copy() {
            if (!Optional.ofNullable(configOperationInfo)
                    .map(ConfigOperationInfo::isCopySupported)
                    .orElse(false)) {
                throw new IllegalArgumentException("Component doesn't support copy.");
            }
            try {
                return clientToolkit.invoke(
                        COPY);
            } catch (Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        @Override
        public void delete() {
            if (!Optional.ofNullable(configOperationInfo)
                    .map(ConfigOperationInfo::isCutSupported)
                    .orElse(false)) {
                throw new IllegalArgumentException("Component doesn't support delete.");
            }
            try {
                clientToolkit.invoke(
                        DELETE);
            } catch (Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        @Override
        public void paste(int index, String config) {
            if (!Optional.ofNullable(configOperationInfo)
                    .map(ConfigOperationInfo::isPasteSupported)
                    .orElse(false)) {
                throw new IllegalArgumentException("Component doesn't support paste. Check supportsPaste");
            }
            try {
                clientToolkit.invoke(
                        PASTE,
                        index,
                        config);
            } catch (Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        @Override
        public QTag[] possibleChildren() {
            if (!Optional.ofNullable(configOperationInfo)
                    .map(ConfigOperationInfo::isPasteSupported)
                    .orElse(false)) {
                throw new IllegalArgumentException("Component doesn't support children. Check supportsPaste");
            }

            PossibleChildren possibleChildren;
            try {
                possibleChildren = clientToolkit.invoke(POSSIBLE_CHILDREN);
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

        @Override
        public void destroy() {
            logger.trace("Being destroyed so removing all {} for {}.", consumers, clientToolkit);
            if (!consumers.isEmpty()) {
                List<Consumer<? super ConfigOperationInfo>> copy = new ArrayList<>(consumers);
                for (Consumer<? super ConfigOperationInfo> consumer : copy) {
                    removeConfigurationSupportsConsumer(consumer);
                }
            }
        }
    }

    static class ServerDragPointHandler implements ServerInterfaceHandler {

        private final ServerSideToolkit toolkit;

        private final Runnable destroyAction;

        private volatile DragPoint dragPoint;

        private volatile Notification<Integer> lastNotification;

        ServerDragPointHandler(Object component,
                               ConfigurationOwner configurationOwner,
                               ServerSideToolkit toolkit) {

            this.toolkit = toolkit;

            Consumer<ConfigurationSession> sessionConsumer = configurationSession -> {
                dragPoint = configurationSession.dragPointFor(component);

                if (dragPoint == null) {
                    return;
                }

                final Notification<Integer> notification =
                        toolkit.createNotification(CONFIG_POINT_INFO_NOTIFICATION_TYPE,
                                ConfigOperationInfo.builder()
                                        .withSupportsConfiguration(true)
                                        .withSupportsCopy(true)
                                        .withSupportsCut(dragPoint.supportsCut())
                                        .withSupportsPaste(dragPoint.supportsPaste())
                                        .build()
                        .getSupportsFlags());

                toolkit.runSynchronized(() -> toolkit.sendNotification(notification));
                lastNotification = notification;
            };

            OwnerStateListener ownerStateListener = event -> {
                switch (event.getChange()) {
                    case SESSION_CREATED:
                        sessionConsumer.accept(event.getSource().provideConfigurationSession());
                        break;
                    case SESSION_DESTROYED:
                        dragPoint = null;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected");
                }
            };

            // Oddjob could be loaded already
            Optional.ofNullable(configurationOwner.provideConfigurationSession())
                    .ifPresent(sessionConsumer);

            // Or it could be loaded later
            configurationOwner.addOwnerStateListener(ownerStateListener);

            toolkit.setNotifierListener(CONFIG_POINT_INFO_NOTIFICATION_TYPE, new NotifierListener<>() {
                @Override
                public void notificationListenerAdded(NotifierListenerEvent<Integer> event) {
                    Optional.ofNullable(lastNotification)
                            .ifPresent(ln -> event.getListener().handleNotification(ln));
                }

                @Override
                public void notificationListenerRemoved(NotifierListenerEvent<Integer> event) {

                }
            });

            destroyAction = () -> configurationOwner.removeOwnerStateListener(ownerStateListener);
        }

        @Override
        public Object invoke(RemoteOperation<?> operation, Object[] params) throws ArooaParseException, NoSuchOperationException {

            if (CUT.equals(operation)) {

                DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
                String copy = dragPoint.cut();
                try {
                    trn.commit();
                } catch (ArooaParseException e) {
                    trn.rollback();
                    throw e;
                }

                return copy;
            }

            if (COPY.equals(operation)) {

                return dragPoint.copy();
            }

            if (DELETE.equals(operation)) {

                DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
                dragPoint.delete();
                try {
                    trn.commit();
                } catch (ArooaParseException e) {
                    trn.rollback();
                    throw e;
                }

                return null;
            }

            if (PASTE.equals(operation)) {

                Integer index = (Integer) params[0];
                String config = (String) params[1];

                DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
                try {
                    dragPoint.paste(index, config);
                    trn.commit();
                } catch (Exception e) {
                    trn.rollback();
                    throw e;
                }

                return null;
            }

            if (POSSIBLE_CHILDREN.equals(operation)) {

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
            }

            throw NoSuchOperationException.of(toolkit.getRemoteId(),
                    operation.getActionName(), operation.getSignature());
        }

        @Override
        public void destroy() {
            destroyAction.run();
        }
    }
}
