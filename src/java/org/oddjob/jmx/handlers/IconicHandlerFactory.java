package org.oddjob.jmx.handlers;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ReflectionException;

import org.oddjob.Iconic;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.IconTip;
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

/**
 * A MBean which wraps an object providing an Oddjob management interface to the
 * object.
 */

public class IconicHandlerFactory 
implements ServerInterfaceHandlerFactory<Iconic, Iconic> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);
	
	public static final String ICON_CHANGED_NOTIF_TYPE = "org.oddjob.iconchanged";

	static final JMXOperationPlus<Notification[]> SYNCHRONIZE = 
		new JMXOperationPlus<Notification[]>(
				"iconicSynchronize", 
				"Sychronize Notifications.", 
				Notification[].class, 
				MBeanOperationInfo.INFO);
		
	static final JMXOperationPlus<IconTip> ICON_FOR = 
		new JMXOperationPlus<IconTip>(
				"Iconic.iconForId",
				"Retrieve an Icon and ToolTip.",
				IconTip.class, 
				MBeanOperationInfo.INFO)
			.addParam("iconId", String.class, "The icon id.");
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jmx.server.InterfaceInfo#interfaceClass()
	 */
	public Class<Iconic> interfaceClass() {
		return Iconic.class;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jmx.server.InterfaceInfo#getMBeanAttributeInfo()
	 */
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jmx.server.InterfaceInfo#getMBeanOperationInfo()
	 */
	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
				SYNCHRONIZE.getOpInfo(),
				ICON_FOR.getOpInfo() };
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jmx.server.InterfaceInfo#getMBeanNotificationInfo()
	 */
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[] {
				new MBeanNotificationInfo(
							new String[] { ICON_CHANGED_NOTIF_TYPE },
							Notification.class.getName(),
							"Icon changed notification.") };
	}
	
	public ServerInterfaceHandler createServerHandler(Iconic iconic, ServerSideToolkit ojmb) {
		ServerIconicHelper iconicHelper = new ServerIconicHelper(iconic, ojmb);
		iconic.addIconListener(iconicHelper);
		return iconicHelper;
	}

	public ClientHandlerResolver<Iconic> clientHandlerFactory() {
		return new SimpleHandlerResolver<Iconic>(
				ClientIconicHandlerFactory.class.getName(), VERSION);
	}
	
	public static class ClientIconicHandlerFactory implements ClientInterfaceHandlerFactory<Iconic> {
		
		public Class<Iconic> interfaceClass() {
			return Iconic.class;
		}
		
		public HandlerVersion getVersion() {
			return VERSION;
		}
		
		public Iconic createClientHandler(Iconic proxy, ClientSideToolkit toolkit) {
			return new ClientIconicHandler(proxy, toolkit);
		}
	}	
	
	static class ClientIconicHandler implements Iconic {
				
		/** Remember the last event so new state listeners can be told it. */
		private IconEvent lastEvent;

		/** listeners */
		private final List<IconListener> listeners = 
			new ArrayList<IconListener>();

		/** The owner, to be used as the source of the event. */
		private final Iconic owner;

		private final ClientSideToolkit toolkit;
		
		private Synchronizer synchronizer;
		
		ClientIconicHandler(Iconic proxy, ClientSideToolkit toolkit) {
			this.owner = proxy;
			this.toolkit = toolkit;
			
			lastEvent = new IconEvent(owner, IconHelper.NULL);
		}
		
		public IconTip iconForId(String id) {
			try {
				return toolkit.invoke(
						ICON_FOR,
						new Object[] { id } );
			}
			catch (Throwable e) {
				throw new UndeclaredThrowableException(e);
			}
		}
		
		void iconEvent(IconData event) {
			// The event that comes over the wire has a null source, so create a new one
			// job node client as the source.
			IconEvent iconEvent = new IconEvent(owner, event.getIconId());

			lastEvent = iconEvent;
			
			synchronized (listeners) {
				for (Iterator<IconListener> it = listeners.iterator(); it.hasNext();) {
					it.next().iconEvent(iconEvent);	
				}
			}
		}
		
		public void addIconListener(IconListener listener) {
			synchronized (this) {
				if (synchronizer == null) {
					
					synchronizer = new Synchronizer(
						new NotificationListener() {
							public void handleNotification(Notification notification, Object arg1) {
								IconData ie = (IconData) notification.getUserData();
								iconEvent(ie);
							}

						});
					toolkit.registerNotificationListener(
							ICON_CHANGED_NOTIF_TYPE, synchronizer);
					
					Notification[] lastNotifications = null;
					try {
						lastNotifications = toolkit.invoke(SYNCHRONIZE);
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
					
					synchronizer.synchronize(lastNotifications);
				}
				
				IconEvent nowEvent = lastEvent;
				synchronized (listeners) {
					listener.iconEvent(nowEvent);
					listeners.add(listener);
				}
			}
		}

		/**
		 * Remove a job state listener.
		 * 
		 * @param listener The job state listener.
		 */	
		public void removeIconListener(IconListener listener) {
			synchronized (this) {
				listeners.remove(listener);
				if (listeners.size() == 0) {
					toolkit.removeNotificationListener(ICON_CHANGED_NOTIF_TYPE, synchronizer);
					synchronizer = null;
				}
			}
		}
	}
	
	/**
	 *
	 *
	 */
	class ServerIconicHelper implements IconListener, ServerInterfaceHandler  {

		private final Iconic iconic;
		private final ServerSideToolkit toolkit;
		
		/** Remember last event. */
		private Notification lastNotification;

		ServerIconicHelper(Iconic iconic, ServerSideToolkit ojmb) {
			this.iconic = iconic;
			this.toolkit = ojmb;
		}
		
		public void iconEvent(final IconEvent event) {
			toolkit.runSynchronized(new Runnable() {
				public void run() {
					// send a dummy source accross the wire
					IconData newEvent = new IconData(event.getIconId());
					Notification notification = 
						toolkit.createNotification(ICON_CHANGED_NOTIF_TYPE);
					notification.setUserData(newEvent);
					toolkit.sendNotification(notification);
					lastNotification = notification;
				}
			});
		}
		
		public Notification[] getLastNotifications() {
			return null;
		}
		
		
		
		public Object invoke(RemoteOperation<?> operation, Object[] params) 
		throws MBeanException, ReflectionException {

			if (ICON_FOR.equals(operation)) {
				return iconic.iconForId((String) params[0]);
			}	
			
			if (SYNCHRONIZE.equals(operation)) {
				return new Notification[] { lastNotification };
			}

			throw new ReflectionException(
					new IllegalStateException("invoked for an unknown method."), 
							operation.toString());
		}
		
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
		 * @param source The source of the event.
		 * @param id The icon id.
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
	}
}