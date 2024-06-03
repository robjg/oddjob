/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.handlers;

import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.*;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.remote.*;
import org.oddjob.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;

public class StatefulHandlerFactory
        implements ServerInterfaceHandlerFactory<Stateful, Stateful> {

    private static final Logger logger = LoggerFactory.getLogger(StatefulHandlerFactory.class);

    public static final HandlerVersion VERSION = new HandlerVersion(5, 0);

    public static final NotificationType<StateData> STATE_CHANGE_NOTIF_TYPE =
            NotificationType.ofName("org.oddjob.statechange")
                    .andDataType(StateData.class);

    @SuppressWarnings({"unchecked", "rawtypes"})
    static final JMXOperationPlus<Notification<StateData>[]> SYNCHRONIZE =
            new JMXOperationPlus(
                    "statefulSynchronize",
                    "Sychronize Notifications.",
                    Notification[].class,
                    MBeanOperationInfo.INFO);

    private static final JMXOperationPlus<StateData> LAST_STATE_EVENT =
            new JMXOperationPlus<>(
                    "lastStateEvent",
                    "Get Last State Event.",
                    StateData.class,
                    MBeanOperationInfo.INFO);

    @Override
    public Class<Stateful> serverClass() {
        return Stateful.class;
    }

    @Override
    public Class<Stateful> clientClass() {
        return Stateful.class;
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
                SYNCHRONIZE.getOpInfo(),
                LAST_STATE_EVENT.getOpInfo(),
        };
    }

    @Override
    public List<NotificationType<?>> getNotificationTypes() {
        return Collections.singletonList(STATE_CHANGE_NOTIF_TYPE);
    }

    @Override
    public ServerInterfaceHandler createServerHandler(Stateful stateful,
                                                      ServerSideToolkit ojmb) {
        ServerStateHandler stateHelper = new ServerStateHandler(stateful, ojmb);
        try {
            stateful.addStateListener(stateHelper);
        } catch (JobDestroyedException e) {
            stateHelper.jobStateChange(stateful.lastStateEvent());
        }
        return stateHelper;
    }

    public static class ClientFactory
            implements ClientInterfaceHandlerFactory<Stateful> {

        @Override
        public Class<Stateful> interfaceClass() {
            return Stateful.class;
        }

        @Override
        public HandlerVersion getVersion() {
            return VERSION;
        }

        @Override
        public Stateful createClientHandler(Stateful proxy, ClientSideToolkit toolkit) {
            return new ClientStatefulHandler(proxy, toolkit);
        }
    }

    /**
     * Implement a remote state listener. This handles remote state events and also
     * propagates them on the client side as normal state events.
     *
     * @author Rob Gordon
     */

    static class ClientStatefulHandler implements Stateful, Destroyable {

        /**
         * Remember the last event so new state listeners can be told it.
         */
        private StateEvent lastEvent;

        /**
         * State listeners
         */
        private final Set<StateListener> listeners =
                new HashSet<>();

        private final ClientSideToolkit toolkit;

        /**
         * The owner, to be used as the source of the event.
         */
        private final Stateful owner;

        private Synchronizer<StateData> synchronizer;

        /**
         * Constructor.
         *
         * @param owner The owning (source) object.
         */
        public ClientStatefulHandler(Stateful owner, ClientSideToolkit toolkit) {
            this.owner = owner;
            this.toolkit = toolkit;
            lastEvent = StateEvent.now(this.owner, JobState.READY);
        }

        StateEvent dataToEvent(StateData data) {
            return StateEvent.fromDetail(owner, data);
        }

        void jobStateChange(StateData data) {

            StateEvent newEvent = dataToEvent(data);

            lastEvent = newEvent;
            List<StateListener> copy;
            synchronized (listeners) {
                copy = new ArrayList<>(listeners);
            }
            for (StateListener listener : copy) {
                listener.jobStateChange(newEvent);
            }
        }

        /**
         * Add a job state listener.
         *
         * @param listener The job state listener.
         */
        @Override
        public void addStateListener(StateListener listener) throws JobDestroyedException {
            synchronized (this) {
                if (synchronizer == null) {

                    synchronizer = new Synchronizer<>(
                            notification -> {
                                StateData stateData = notification.getData();
                                jobStateChange(stateData);
                            });

                    Notification<StateData>[] lastNotifications;

                    try {
                        toolkit.registerNotificationListener(
                                STATE_CHANGE_NOTIF_TYPE, synchronizer);

                        logger.trace("Created new Synchronizer for {}, toolkit {}", STATE_CHANGE_NOTIF_TYPE, toolkit);

                        lastNotifications = toolkit.invoke(SYNCHRONIZE);
                    } catch (RemoteUnknownException e) {
                        throw new JobDestroyedException(owner);
                    } catch (RemoteException e) {
                        throw new RemoteRuntimeException(e);
                    }

                    synchronizer.synchronize(lastNotifications);
                }

                if (lastEvent.getState().isDestroyed()) {
                    throw new JobDestroyedException(owner);
                }

                StateEvent nowEvent = lastEvent;
                listener.jobStateChange(nowEvent);
                if (listeners.add(listener)) {
                    logger.trace("Added new State Listener {}, toolkit {}", listener, toolkit);
                } else {
                    logger.trace("State Listener {} notified but not added as it exists already, toolkit {}",
                            listener, toolkit);
                }
            }
        }

        /**
         * Remove a job state listener.
         *
         * @param listener The job state listener.
         */
        public void removeStateListener(StateListener listener) {
            synchronized (this) {
                if (listeners.remove(listener)) {
                    int num = listeners.size();
                    if (num == 0) {
                        try {
                            toolkit.removeNotificationListener(STATE_CHANGE_NOTIF_TYPE, synchronizer);
                        } catch (RemoteException e) {
                            throw new RemoteRuntimeException(e);
                        }
                        synchronizer = null;
                        logger.trace("Removed last State Listener {}, toolkit {}", listener, toolkit);
                    } else {
                        logger.trace("Removed State Listener {}, toolkit {}, {} remain", listener, toolkit, num);
                    }
                } else {
                    logger.trace("State Listener {} not removed from toolkit {}, as it was not registered.",
                            listener, toolkit);
                }
            }
        }

        @Override
        public StateEvent lastStateEvent() {
            synchronized (this) {
                if (!listeners.isEmpty()) {
                    return lastEvent;
                }
            }
            try {
                return dataToEvent(toolkit.invoke(LAST_STATE_EVENT));
            } catch (Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        @Override
        public void destroy() {
            jobStateChange(new StateData(
                    new ClientDestroyed(), StateInstant.now(), null));
            logger.trace("Being destroyed so removing all {} listeners for {}.", listeners, toolkit);
            synchronized (this) {
                if (!listeners.isEmpty()) {
                    Set<StateListener> copy = new HashSet<>(listeners);
                    for (StateListener listener : copy) {
                        removeStateListener(listener);
                    }
                }
            }
        }
    }

    static class ServerStateHandler implements StateListener, ServerInterfaceHandler {

        private final Stateful stateful;
        private final ServerSideToolkit toolkit;

        /**
         * Remember last event.
         */
        private Notification<StateData> lastNotification;

        ServerStateHandler(Stateful stateful,
                           ServerSideToolkit ojmb) {
            this.stateful = stateful;
            this.toolkit = ojmb;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.oddjob.state.AbstractJobStateListener#jobStateChange(org.oddjob.state.JobStateEvent)
         */
        @Override
        public void jobStateChange(final StateEvent event) {
            toolkit.runSynchronized(() -> {
                StateData newEvent = new StateData(
                        event.getState(),
                        event.getStateInstant(),
                        event.getException());
                Notification<StateData> notification =
                        toolkit.createNotification(STATE_CHANGE_NOTIF_TYPE, newEvent);
                toolkit.sendNotification(notification);
                lastNotification = notification;
            });
        }

        @Override
        public Object invoke(RemoteOperation<?> operation, Object[] params)
                throws RemoteException {

            if (SYNCHRONIZE.equals(operation)) {
                return new Notification[]{lastNotification};
            }

            if (LAST_STATE_EVENT.equals(operation)) {
                return lastNotification.getData();
            }

            throw NoSuchOperationException.of(toolkit.getRemoteId(),
                    operation.getActionName(), operation.getSignature());
        }

        @Override
        public void destroy() {
            stateful.removeStateListener(this);
        }
    }

    public static class StateData implements Serializable, StateDetail {
        private static final long serialVersionUID = 2023061500L;

        private final GenericState state;

        private final StateInstant instant;

        private final OddjobTransportableException exception;

        @Deprecated(since = "1.7", forRemoval = true)
        public StateData(State state, Date date, Throwable throwable) {
            this(state, StateInstant.forOneVersionOnly(date.toInstant()), throwable);
        }

        public StateData(State state, StateInstant instant, Throwable throwable) {
            this.state = GenericState.from(state);
            this.instant = instant;
            this.exception = OddjobTransportableException.from(throwable);
        }

        @Deprecated(since = "1.7", forRemoval = true)
        public State getJobState() {
            return state;
        }

        @Override
        public State getState() {
            return state;
        }

        @Deprecated(since = "1.7", forRemoval = true)
        public Date getDate() {
            return Date.from(instant.getInstant());
        }

        @Override
        public StateInstant getStateInstant() {
            return instant;
        }

        @Deprecated(since = "1.7", forRemoval = true)
        public Throwable getThrowable() {
            return exception;
        }

        @Override
        public OddjobTransportableException getException() {
            return exception;
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

