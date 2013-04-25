/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.handlers;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.log4j.Logger;
import org.oddjob.Structural;
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
import org.oddjob.jmx.server.ServerLoopBackException;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.ChildMatch;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

public class StructuralHandlerFactory 
implements ServerInterfaceHandlerFactory<Structural, Structural> {
	
	private static final Logger logger = Logger.getLogger(StructuralHandlerFactory.class);
	
	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
	
	public static final String STRUCTURAL_NOTIF_TYPE = "org.oddjob.structural";

	static final JMXOperationPlus<Notification[]>  SYNCHRONIZE = 
		new JMXOperationPlus<Notification[]>(
				"structuralSynchronize",
				"Synchronize Notifications.",
				Notification[].class,
				MBeanOperationInfo.INFO);
	
	public Class<Structural> interfaceClass() {
		return Structural.class;
	}
	
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
				SYNCHRONIZE.getOpInfo()
		};
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {

		MBeanNotificationInfo[] nInfo = new MBeanNotificationInfo[] {
				new MBeanNotificationInfo(new String[] {
						STRUCTURAL_NOTIF_TYPE },
						Notification.class.getName(), "Structural notification.")};
		return nInfo;
	}

	public ServerInterfaceHandler createServerHandler(
			Structural structural, 
			ServerSideToolkit ojmb) {
		
		ServerStructuralHelper structuralHelper = 
			new ServerStructuralHelper (structural, ojmb);
		
		return structuralHelper;
	}
	
	public ClientHandlerResolver<Structural> clientHandlerFactory() {
		return new SimpleHandlerResolver<Structural>(
				ClientStructuralHandlerFactory.class.getName(),
				VERSION);
	}
	
	public static class ClientStructuralHandlerFactory 
	implements ClientInterfaceHandlerFactory<Structural> {
		
		public Class<Structural> interfaceClass() {
			return Structural.class;
		}
		
		public HandlerVersion getVersion() {
			return VERSION;
		}
		
		public Structural createClientHandler(Structural proxy, ClientSideToolkit toolkit) {
			return new ClientStructuralHandler(proxy, toolkit);
		}		
	}
	
	static class ClientStructuralHandler 
	implements Structural {

		private final Structural proxy;
		
		/** Helper class to handle structure change */
		private ChildHelper<Object> structuralHelper;

		private final ClientSideToolkit toolkit;

		private Synchronizer synchronizer;
		
		private List<ObjectName> childNames;
		
		ClientStructuralHandler(Structural proxy, ClientSideToolkit toolkit) {
			this.proxy = proxy;
			this.toolkit = toolkit;
		}
		
		/*
		 * Add a structural listener. From the Structural interface.
		 */
		public void addStructuralListener(StructuralListener listener) {
			synchronized (this) {
				if (structuralHelper == null) {
					this.structuralHelper = new ChildHelper<Object>(proxy);			
					this.childNames = new ArrayList<ObjectName>();
					
					synchronizer = new Synchronizer(
						new NotificationListener() {
							public void handleNotification(Notification notification, Object arg1) {
								ChildData childData = (ChildData) notification.getUserData();
								
								new ChildMatch<ObjectName>(childNames) {
									protected void insertChild(int index, ObjectName childName) {
										Object childProxy = toolkit.getClientSession().create(childName);
										// child proxy will be null if the toolkit can't create it.
										if (childProxy != null) {
											structuralHelper.insertChild(index, childProxy);
										}
									};
									@Override
									protected void removeChildAt(int index) {
										Object child = structuralHelper.removeChildAt(index);
										toolkit.getClientSession().destroy(child);
									}
								}.match(childData.getChildObjectNames());
							}

						});
					toolkit.registerNotificationListener(
							STRUCTURAL_NOTIF_TYPE, synchronizer);
					
					Notification[] lastNotifications = null;
					try {
						lastNotifications = toolkit.invoke(SYNCHRONIZE);
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
					
					synchronizer.synchronize(lastNotifications);
				}
			}
			
			structuralHelper.addStructuralListener(listener);
		}

		/*
		 * Remove a structural listener. From the Structural interface.
		 */
		public void removeStructuralListener(StructuralListener listener) {
			synchronized (this) {
				if (structuralHelper != null) {
					structuralHelper.removeStructuralListener(listener);
					
					if (structuralHelper.isNoListeners()) {
						toolkit.removeNotificationListener(STRUCTURAL_NOTIF_TYPE, synchronizer);
						synchronizer = null;
						structuralHelper = null;
					}
				}
			}
		}		
	}	
	
	class ServerStructuralHelper implements ServerInterfaceHandler  {

		private final Structural structural;
		private final ServerSideToolkit toolkit;
		
		/** True if we've looped back onto an already exposed server. */
		private boolean duplicate;

		/** Child remote job nodes. */
		private final LinkedList<ObjectName> children = new LinkedList<ObjectName>();

		private final StructuralListener listener = new StructuralListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.oddjob.structural.StructuralListener#childAdded(org.oddjob.structural.StructuralEvent)
			 */
			public void childAdded(final StructuralEvent e) {
				// stop events overlapping.
				final ObjectName child;
				Object childComponent = e.getChild();
				try {
					child = toolkit.getServerSession().createMBeanFor(
							childComponent, toolkit.getContext().addChild(childComponent));
				} catch (ServerLoopBackException e1) {
					logger.info("Server loopback detected.");
					duplicate = true;
					return;
				} catch (JMException e2) {
					logger.error("Failed creating child for [" + childComponent + "]", e2);
					return;
				}
				final int index = e.getIndex();
				
				ChildData newEvent = null;
				
				synchronized (children) {
					children.add(index, child);
					newEvent = new ChildData(
						children.toArray(new ObjectName[children.size()]));
				}
				
				final Notification notification = 
					toolkit.createNotification(STRUCTURAL_NOTIF_TYPE);
				notification.setUserData(newEvent);
				
				toolkit.runSynchronized(new Runnable() {
					public void run() {
						toolkit.sendNotification(notification);					
					}
				});
				
				logger.debug("Child added [" + e.getChild().toString() + "], index [" + e.getIndex() + "]");
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.oddjob.structural.StructuralListener#childRemoved(org.oddjob.structural.StructuralEvent)
			 */
			public void childRemoved(final StructuralEvent e) {
				if (duplicate) {
					// a duplicate was never added.
					return;
				}
				final int index = e.getIndex();
				
				ObjectName child = null;
				ChildData newEvent = null;
				
				synchronized(children) {
					
					child = children.get(index);
					children.remove(index);
					newEvent = new ChildData(
							children.toArray(new ObjectName[children.size()]));
				}
				
				final Notification notification = 
					toolkit.createNotification(STRUCTURAL_NOTIF_TYPE);
				notification.setUserData(newEvent);

				toolkit.runSynchronized(new Runnable() {
					public void run() {
						toolkit.sendNotification(notification);
					}
				});
				
				try {
					toolkit.getServerSession().destroy(child);
				} catch (JMException e1) {
					logger.error("Failed destroying child [" + e.getChild() + "]", e1);
				}
				logger.debug("Child removed [" + e.getChild().toString() + "], index [" + e.getIndex() + "]");
			}
		};
		
		ServerStructuralHelper(Structural structural, 
				ServerSideToolkit ojmb) {
			this.structural = structural;
			this.toolkit = ojmb;
			structural.addStructuralListener(listener);
		}

		private Notification[] lastNotifications() {
			final Notification[] lastNotifications = new Notification[1];
			toolkit.runSynchronized(new Runnable() {
				public void run() {
					ChildData newEvent = new ChildData(
							children.toArray(new ObjectName[children.size()]));
					Notification notification = 
						toolkit.createNotification(STRUCTURAL_NOTIF_TYPE);
					notification.setUserData(newEvent);
					lastNotifications[0] = notification;
				}
			});
			
			return lastNotifications;
		}

		public Object invoke(RemoteOperation<?> operation, Object[] params) throws MBeanException, ReflectionException {
		
			
			if (SYNCHRONIZE.equals(operation)) {
				return lastNotifications();
			}

			throw new ReflectionException(
					new IllegalStateException("invoked for an unknown method."), 
							operation.toString());
		}
		
		public void destroy() {
			
			// Stop receiving event from Oddjob
			structural.removeStructuralListener(listener);
			
			// And use our listener to remove children.
			while (children.size() > 0) {
				final int index = children.size() - 1;
				StructuralEvent dummyEvent = new StructuralEvent(structural,
						new Object() {
							@Override
							public String toString() {
								return "Dummy Child " + index + 
										" of Destructing Parent [" +
										structural + "]";
							}
						}, index);
				listener.childRemoved(dummyEvent);
			}
		}

	}

	
	static class ChildData implements Serializable {
		private static final long serialVersionUID = 2010062500L;
		
		private final ObjectName[] objectNames;
		
		public ChildData(ObjectName[] objectName) {
			this.objectNames = objectName;
		}
		
		public ObjectName[] getChildObjectNames() {
			return objectNames;
		}
	}
	
}
