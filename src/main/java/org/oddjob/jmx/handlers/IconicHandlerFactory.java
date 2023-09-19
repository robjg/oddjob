package org.oddjob.jmx.handlers;

import org.oddjob.Iconic;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.ImageData;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.*;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import java.io.Serializable;
import java.util.*;

/**
 * A MBean which wraps an object providing an Oddjob management interface to the
 * object.
 */

public class IconicHandlerFactory
        implements ServerInterfaceHandlerFactory<Iconic, Iconic> {

    private static final Logger logger = LoggerFactory.getLogger(IconicHandlerFactory.class);

    public static final HandlerVersion VERSION = new HandlerVersion(4, 0);

    public static final NotificationType<IconData> ICON_CHANGED_NOTIF_TYPE =
            NotificationType.ofName("org.oddjob.iconchanged")
                    .andDataType(IconData.class);

    @SuppressWarnings({"unchecked", "rawtypes"})
    static final JMXOperationPlus<Notification<IconData>> SYNCHRONIZE =
            new JMXOperationPlus(
                    "iconicSynchronize",
                    "Sychronize Notifications.",
                    Notification.class,
                    MBeanOperationInfo.INFO);

    static final JMXOperationPlus<ImageData> ICON_FOR =
            new JMXOperationPlus<>(
                    "Iconic.iconForId",
                    "Retrieve an Icon and ToolTip.",
                    ImageData.class,
                    MBeanOperationInfo.INFO)
                    .addParam("iconId", String.class, "The icon id.");

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jmx.server.InterfaceInfo#interfaceClass()
     */
    @Override
    public Class<Iconic> serverClass() {
        return Iconic.class;
    }

    @Override
    public Class<Iconic> clientClass() {
        return Iconic.class;
    }

    @Override
    public HandlerVersion getHandlerVersion() {
        return VERSION;
    }

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jmx.server.InterfaceInfo#getMBeanAttributeInfo()
     */
    @Override
    public MBeanAttributeInfo[] getMBeanAttributeInfo() {
        return new MBeanAttributeInfo[0];
    }

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jmx.server.InterfaceInfo#getMBeanOperationInfo()
     */
    @Override
    public MBeanOperationInfo[] getMBeanOperationInfo() {
        return new MBeanOperationInfo[]{
                SYNCHRONIZE.getOpInfo(),
                ICON_FOR.getOpInfo()};
    }


    @Override
    public List<NotificationType<?>> getNotificationTypes() {
        return Collections.singletonList(ICON_CHANGED_NOTIF_TYPE);
    }

    @Override
    public ServerInterfaceHandler createServerHandler(Iconic iconic, ServerSideToolkit ojmb) {
        ServerIconicHelper iconicHelper = new ServerIconicHelper(iconic, ojmb);
        iconic.addIconListener(iconicHelper);
        return iconicHelper;
    }

    public static class ClientFactory implements ClientInterfaceHandlerFactory<Iconic> {

        @Override
        public Class<Iconic> interfaceClass() {
            return Iconic.class;
        }

        @Override
        public HandlerVersion getVersion() {
            return VERSION;
        }

        @Override
        public Iconic createClientHandler(Iconic proxy, ClientSideToolkit toolkit) {
            return new ClientIconicHandler(proxy, toolkit);
        }
    }

    public static class ClientIconicHandler implements Iconic, Destroyable {

        /**
         * Remember the last event so new state listeners can be told it.
         */
        private IconEvent lastEvent;

        /**
         * listeners
         */
        private final Set<IconListener> listeners =
                new HashSet<>();

        /**
         * The owner, to be used as the source of the event.
         */
        private final Iconic owner;

        private final ClientSideToolkit toolkit;

        private Synchronizer<IconData> synchronizer;

        ClientIconicHandler(Iconic proxy, ClientSideToolkit toolkit) {
            this.owner = proxy;
            this.toolkit = toolkit;

            lastEvent = new IconEvent(owner, IconHelper.NULL);
        }

        @Override
        public ImageData iconForId(String id) {
            try {
                return toolkit.invoke(
                        ICON_FOR,
                        id);
            } catch (RemoteException e) {
                throw new RemoteRuntimeException(e);
            }
        }

        void iconEvent(IconData event) {
            // The event that comes over the wire has a null source, so create a new one
            // job node client as the source.
            IconEvent iconEvent = new IconEvent(owner, event.getIconId());

            lastEvent = iconEvent;

            synchronized (listeners) {
                for (IconListener listener : listeners) {
                    listener.iconEvent(iconEvent);
                }
            }
        }

        @Override
        public void addIconListener(IconListener listener) {
            synchronized (this) {
                if (synchronizer == null) {

                    synchronizer = new Synchronizer<>(
                            notification -> {
                                IconData ie = notification.getData();
                                iconEvent(ie);
                            });
                    try {
                        toolkit.registerNotificationListener(
                                ICON_CHANGED_NOTIF_TYPE, synchronizer);
                    }
                    catch (RemoteException e) {
                        throw new RemoteRuntimeException(e);
                    }

                    Notification<IconData> lastNotification;
                    try {
                        lastNotification = toolkit.invoke(SYNCHRONIZE);
                    } catch (RemoteException e) {
                        throw new RemoteRuntimeException(e);
                    }

                    synchronizer.synchronize(lastNotification);
                }

                IconEvent nowEvent = lastEvent;
                listener.iconEvent(nowEvent);
                listeners.add(listener);
            }
        }

        /**
         * Remove an Icon Listener.
         *
         * @param listener The Icon Listener.
         */
        @Override
        public void removeIconListener(IconListener listener) {
            synchronized (this) {
                if (listeners.remove(Objects.requireNonNull(listener)) && listeners.size() == 0) {
                    try {
                        toolkit.removeNotificationListener(ICON_CHANGED_NOTIF_TYPE, synchronizer);
                    }
                    catch (RemoteException e) {
                        throw new RemoteRuntimeException(e);
                    }
                    finally {
                        synchronizer = null;
                    }
                }
            }
        }

        @Override
        public void destroy() {
            logger.trace("Being destroyed so removing all {} listeners for {}.", listeners, toolkit);
            synchronized (this) {
                if (!listeners.isEmpty()) {
                    Set<IconListener> copy = new HashSet<>(listeners);
                    for (IconListener listener : copy) {
                        removeIconListener(listener);
                    }
                }
            }
        }
    }

    /**
     *
     */
    static class ServerIconicHelper implements IconListener, ServerInterfaceHandler {

        private final Iconic iconic;
        private final ServerSideToolkit toolkit;

        /**
         * Remember last event.
         */
        private Notification<IconData> lastNotification;

        ServerIconicHelper(Iconic iconic, ServerSideToolkit ojmb) {
            this.iconic = iconic;
            this.toolkit = ojmb;
        }

        @Override
        public void iconEvent(final IconEvent event) {
            toolkit.runSynchronized(() -> {
                // send a dummy source accross the wire
                IconData newEvent = new IconData(event.getIconId());
                Notification<IconData> notification =
                        toolkit.createNotification(ICON_CHANGED_NOTIF_TYPE, newEvent);
                toolkit.sendNotification(notification);
                lastNotification = notification;
            });
        }

        @Override
        public Object invoke(RemoteOperation<?> operation, Object[] params) throws NoSuchOperationException {

            if (ICON_FOR.equals(operation)) {
                return iconic.iconForId((String) params[0]);
            }

            if (SYNCHRONIZE.equals(operation)) {
                return lastNotification;
            }

            throw NoSuchOperationException.of(toolkit.getRemoteId(),
                    operation.getActionName(), operation.getSignature());
        }

        @Override
        public void destroy() {
            iconic.removeIconListener(this);
        }
    }

    public static class IconData implements Serializable {
        private static final long serialVersionUID = 2009062400L;

        final private String id;

        /**
         * Event constructor.
         *
         * @param iconId The icon id.
         */
        public IconData(String iconId) {

            this.id = iconId;
        }

        /**
         * Get the variable name.
         *
         * @return The variable name.
         */

        public String getIconId() {

            return id;
        }

        @Override
        public String toString() {
            return "IconData{" +
                    "id='" + id + '\'' +
                    '}';
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