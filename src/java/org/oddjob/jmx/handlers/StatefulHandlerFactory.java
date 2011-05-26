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

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ReflectionException;

import org.oddjob.Stateful;
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
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

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
		stateful.addJobStateListener(stateHelper);
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
		private JobStateEvent lastEvent;

		/** State listeners */
		private final List<JobStateListener> listeners = 
			new ArrayList<JobStateListener>();
		
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
			lastEvent = new JobStateEvent(this.owner, JobState.READY, null);	
		}

		void jobStateChange(StateData data) {
			
			JobStateEvent newEvent = new JobStateEvent(owner, data.getJobState(),
					data.getDate(), data.getThrowable());

			lastEvent = newEvent;
			List<JobStateListener> copy = null;
			synchronized (listeners) {
				copy = new ArrayList<JobStateListener>(listeners);
			}
			for (Iterator<JobStateListener> it = copy.iterator(); it.hasNext();) {				
				((JobStateListener)it.next()).jobStateChange(newEvent);	
			}
		}
		
		/**
		 * Add a job state listener.
		 * 
		 * @param listener The job state listener.
		 */
		public void addJobStateListener(JobStateListener listener) {	
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
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
					
					synchronizer.synchronize(lastNotifications);
				}
				
				JobStateEvent nowEvent = lastEvent;
				listener.jobStateChange(nowEvent);
				listeners.add(listener);
			}
		}

		/**
		 * Remove a job state listener.
		 * 
		 * @param listener The job state listener.
		 */
		public void removeJobStateListener(JobStateListener listener) {
			synchronized (this) {
				listeners.remove(listener);
				if (listeners.size() == 0) {
					toolkit.removeNotificationListener(STATE_CHANGE_NOTIF_TYPE, synchronizer);
					synchronizer = null;
				}
			}
		}
		
		@Override
		public JobStateEvent lastJobStateEvent() {
			return lastEvent;
		}
	}

	
	class ServerStateHandler implements JobStateListener, ServerInterfaceHandler  {

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
		public void jobStateChange(final JobStateEvent event) {
			toolkit.runSynchronized(new Runnable() {
				public void run() {
					StateData newEvent = new StateData(
							event.getJobState(), 
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
		
		public Notification[] getLastNotifications() {
			return null;
		}
		
		public void destroy() {
			stateful.removeJobStateListener(this);
		}
	}
	
	public static class StateData implements Serializable {
		private static final long serialVersionUID = 2009063000L;

		private final JobState jobState;
		
		private final Date date;
		
		private final Throwable throwable;
		
		public StateData(JobState state, Date date, Throwable throwable) {
			this.jobState = state;
			this.date = date;
			this.throwable = throwable;
		}
		
		public JobState getJobState() {
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
