/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.handlers;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ReflectionException;

import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.client.SimpleHandlerResolver;
import org.oddjob.jmx.client.Synchronizer;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.state.JobState;
import org.oddjob.state.StateListener;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;

public class StatefulHandlerFactory 
implements ServerInterfaceHandlerFactory<Stateful, Stateful> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
	
	public static final String STATE_CHANGE_NOTIF_TYPE = "org.oddjob.statechange";

	static final JMXOperationPlus<Notification[]> SYNCHRONIZE = 
		new JMXOperationPlus<Notification[]>(
				"statefulSynchronize", 
				"Sychronize Notifications.", 
				Notification[].class, 
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
			};
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {

		MBeanNotificationInfo[] nInfo = new MBeanNotificationInfo[] {
					new MBeanNotificationInfo(
							new String[] { STATE_CHANGE_NOTIF_TYPE },
							Notification.class.getName(),
							"State change notification.") };
		return nInfo;
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

	public ClientHandlerResolver<Stateful> clientHandlerFactory() {
		return new SimpleHandlerResolver<Stateful>(
				ClientStatefulHandlerFactory.class.getName(),
				VERSION);
	}
	
	public static class ClientStatefulHandlerFactory 
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

	static class ClientStatefulHandler implements Stateful {

		/** Remember the last event so new state listeners can be told it. */
		private StateEvent lastEvent;

		/** State listeners */
		private final List<StateListener> listeners = 
			new ArrayList<StateListener>();
		
		private final ClientSideToolkit toolkit;

		/** The owner, to be used as the source of the event. */
		private final Stateful owner;

		private Synchronizer synchronizer;
		
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

		void jobStateChange(StateData data) {
			
			StateEvent newEvent = new StateEvent(owner, data.getJobState(),
					data.getDate(), data.getThrowable());

			lastEvent = newEvent;
			List<StateListener> copy = null;
			synchronized (listeners) {
				copy = new ArrayList<StateListener>(listeners);
			}
			for (Iterator<StateListener> it = copy.iterator(); it.hasNext();) {				
				((StateListener)it.next()).jobStateChange(newEvent);	
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
					
					synchronizer = new Synchronizer(
						new NotificationListener() {
							public void handleNotification(Notification notification, Object arg1) {
								StateData stateData = (StateData) notification.getUserData();
								jobStateChange(stateData);
							}

						});
					toolkit.registerNotificationListener(
							STATE_CHANGE_NOTIF_TYPE, synchronizer);
					
					Notification[] lastNotifications = null;
					try {
						lastNotifications = (Notification[]) toolkit.invoke(SYNCHRONIZE);
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
			return lastEvent;
		}
	}

	
	class ServerStateHandler implements StateListener, ServerInterfaceHandler  {

		private final Stateful stateful;
		private final ServerSideToolkit toolkit;
		
		/** Remember last event. */
		private Notification lastNotification;

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
			toolkit.runSynchronized(new Runnable() {
				public void run() {
					StateData newEvent = new StateData(
							event.getState(), 
							event.getTime(), 
							event.getException());
					Notification notification = 
						toolkit.createNotification(STATE_CHANGE_NOTIF_TYPE);
					notification.setUserData(newEvent);
					toolkit.sendNotification(notification);
					lastNotification = notification;					
				}
			});
		}

		public Object invoke(RemoteOperation<?> operation, Object[] params) 
		throws MBeanException, ReflectionException {
			
			if (SYNCHRONIZE.equals(operation)) {
				return new Notification[] { lastNotification };
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

		private final State jobState;
		
		private final Date date;
		
		private final Throwable throwable;
		
		public StateData(State state, Date date, Throwable throwable) {
			this.jobState = state;
			this.date = date;
			this.throwable = throwable;
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
}
