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
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationType;
import org.oddjob.state.*;

import javax.management.*;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatefulHandlerFactory 
implements ServerInterfaceHandlerFactory<Stateful, Stateful> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(3, 0);
	
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
		
	public Class<Stateful> interfaceClass() {
		return Stateful.class;
	}
	
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
				SYNCHRONIZE.getOpInfo(),
				LAST_STATE_EVENT.getOpInfo(),
			};
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {

		return new MBeanNotificationInfo[] {
					new MBeanNotificationInfo(
							new String[] { STATE_CHANGE_NOTIF_TYPE.getName() },
							Notification.class.getName(),
							"State change notification.") };
	}

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

	public Class<Stateful> clientClass() {
		return Stateful.class;
	}
	
	public static class ClientFactory
	implements ClientInterfaceHandlerFactory<Stateful> {
		
		public Class<Stateful> interfaceClass() {
			return Stateful.class;
		}
		
		public HandlerVersion getVersion() {
			return VERSION;
		}
		
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

		/** Remember the last event so new state listeners can be told it. */
		private StateEvent lastEvent;

		/** State listeners */
		private final List<StateListener> listeners =
				new ArrayList<>();
		
		private final ClientSideToolkit toolkit;

		/** The owner, to be used as the source of the event. */
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
			lastEvent = new StateEvent(this.owner, JobState.READY, null);	
		}

		StateEvent dataToEvent(StateData data) {
			return new StateEvent(owner, data.getJobState(),
					data.getDate(), data.getThrowable());
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
		public void addStateListener(StateListener listener) throws JobDestroyedException {	
			synchronized (this) {
				if (synchronizer == null) {
					
					synchronizer = new Synchronizer<>(
							notification -> {
								StateData stateData = notification.getData();
								jobStateChange(stateData);
							});
					toolkit.registerNotificationListener(
							STATE_CHANGE_NOTIF_TYPE, synchronizer);
					
					Notification<StateData>[] lastNotifications;
					try {
						lastNotifications = toolkit.invoke(SYNCHRONIZE);
					}
					catch (InstanceNotFoundException e) {
						throw new JobDestroyedException(owner);
					}
					catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
					
					synchronizer.synchronize(lastNotifications);
				}
				
				if (lastEvent.getState().isDestroyed()) {
					throw new JobDestroyedException(owner);
				}
				
				StateEvent nowEvent = lastEvent;
				listener.jobStateChange(nowEvent);
				listeners.add(listener);
			}
		}

		/**
		 * Remove a job state listener.
		 * 
		 * @param listener The job state listener.
		 */
		public void removeStateListener(StateListener listener) {
			synchronized (this) {
				listeners.remove(listener);
				if (listeners.size() == 0) {
					toolkit.removeNotificationListener(STATE_CHANGE_NOTIF_TYPE, synchronizer);
					synchronizer = null;
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
			}
			catch (Throwable e) {
				throw new UndeclaredThrowableException(e);
			}
		}
		
		@Override
		public void destroy() {
			jobStateChange(new StateData(
					new ClientDestroyed(), new Date(), null)); 
		}
	}

	
	static class ServerStateHandler implements StateListener, ServerInterfaceHandler  {

		private final Stateful stateful;
		private final ServerSideToolkit toolkit;
		
		/** Remember last event. */
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
		public void jobStateChange(final StateEvent event) {
			toolkit.runSynchronized(() -> {
				StateData newEvent = new StateData(
						event.getState(),
						event.getTime(),
						event.getException());
				Notification<StateData> notification =
					toolkit.createNotification(STATE_CHANGE_NOTIF_TYPE, newEvent);
				toolkit.sendNotification(notification);
				lastNotification = notification;
			});
		}

		public Object invoke(RemoteOperation<?> operation, Object[] params) 
		throws MBeanException, ReflectionException {
			
			if (SYNCHRONIZE.equals(operation)) {
				return new Notification[] { lastNotification };
			}

			if (LAST_STATE_EVENT.equals(operation)) {
				return lastNotification.getData();
			}
			throw new ReflectionException(
					new IllegalStateException("invoked for an unknown method."), 
							operation.toString());
		}
		
		public void destroy() {
			stateful.removeStateListener(this);
		}
	}
	
	public static class StateData implements Serializable {
		private static final long serialVersionUID = 2009063000L;

		private final GenericState jobState;
		
		private final Date date;
		
		private final Throwable throwable;
		
		public StateData(State state, Date date, Throwable throwable) {
			this.jobState = GenericState.from(state);
			this.date = date;
			if (throwable == null) {
				this.throwable = null;
			}
			else {
				this.throwable = new OddjobTransportableException(throwable);
			}
		}
		
		public State getJobState() {
			return jobState;
		}
		
		public Date getDate() {
			return date;
		}
		
		public Throwable getThrowable() {
			return throwable;
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

