package org.oddjob.jmx.handlers;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ReflectionException;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.ConfigOwnerEvent;
import org.oddjob.arooa.parsing.ConfigSessionEvent;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.parsing.ConfigurationOwnerSupport;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.parsing.ConfigurationSessionSupport;
import org.oddjob.arooa.parsing.CutAndPasteSupport;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.parsing.OwnerStateListener;
import org.oddjob.arooa.parsing.SerializableDesignFactory;
import org.oddjob.arooa.parsing.SessionStateListener;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.client.SimpleHandlerResolver;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;

/**
 * 
 * @author rob
 */
public class ComponentOwnerHandlerFactory 
implements ServerInterfaceHandlerFactory<ConfigurationOwner, ConfigurationOwner> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(3, 0);
	
	public static final String MODIFIED_NOTIF_TYPE = "oddjob.config.modified";
	
	public static final String CHANGE_NOTIF_TYPE = "oddjob.config.changed";
	
	private static final JMXOperationPlus<Integer> SESSION_AVAILABLE = 
		new JMXOperationPlus<Integer>(
				"ConfigurationOwner.sessionAvailable",
				"",
				Integer.class,
				MBeanOperationInfo.INFO
			);
	
	private static final JMXOperationPlus<DragPointInfo> DRAG_POINT_INFO = 
		new JMXOperationPlus<DragPointInfo>(
				"dragPointInfo",
				"",
				DragPointInfo.class,
				MBeanOperationInfo.INFO
			).addParam("component", Object.class, "");
						
	private static final JMXOperationPlus<Void> CUT = 
		new JMXOperationPlus<Void>(
				"configCut",
				"",
				Void.TYPE,
				MBeanOperationInfo.ACTION_INFO
			).addParam("component", Object.class, "");
			
	private static final JMXOperationPlus<String> PASTE = 
		new JMXOperationPlus<String>(
				"configPaste",
				"",
				String.class,
				MBeanOperationInfo.ACTION
			).addParam(
					"component", Object.class, "").addParam( 
					"index", Integer.TYPE, "").addParam(
					"config", String.class, "");
	
	private static final JMXOperationPlus<Boolean> IS_MODIFIED = 
		new JMXOperationPlus<Boolean>(
				"configIsModified",
				"",
				Boolean.class,
				MBeanOperationInfo.INFO);   
	
	private static final JMXOperationPlus<String> SAVE = 
		new JMXOperationPlus<String>(
				"configSave",
				"",
				String.class,
				MBeanOperationInfo.ACTION);   
	
	private static final JMXOperationPlus<Void> REPLACE = 
		new JMXOperationPlus<Void>(
				"configReplace",
				"",
				Void.TYPE,
				MBeanOperationInfo.INFO
			).addParam("component", Object.class, "");
	
	private static final JMXOperationPlus<ComponentOwnerInfo> INFO = 
		new JMXOperationPlus<ComponentOwnerInfo>(
				"componentOwnerInfo",
				"Basic Info For Component Owner",
				ComponentOwnerInfo.class,
				MBeanOperationInfo.INFO
			);
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.jmx.server.ServerInterfaceHandlerFactory#interfaceClass()
	 */
	public Class<ConfigurationOwner> interfaceClass() {
		return ConfigurationOwner.class;
	}
	
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
			INFO.getOpInfo(),
			SESSION_AVAILABLE.getOpInfo(),
			DRAG_POINT_INFO.getOpInfo(),
			CUT.getOpInfo(),
			PASTE.getOpInfo(),
			SAVE.getOpInfo(),
			IS_MODIFIED.getOpInfo(),
			REPLACE.getOpInfo()
		};
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		MBeanNotificationInfo[] nInfo = new MBeanNotificationInfo[] {
				new MBeanNotificationInfo(new String[] {
						MODIFIED_NOTIF_TYPE },
						Notification.class.getName(), "Modified Notification.")};
		return nInfo;
	}	

	public ServerInterfaceHandler createServerHandler(
			ConfigurationOwner target, ServerSideToolkit ojmb) {
		return new ServerComponentOwnerHandler(target, ojmb);
	}

	public ClientHandlerResolver<ConfigurationOwner> clientHandlerFactory() {
		return new SimpleHandlerResolver<ConfigurationOwner>(
				ClientConfigurationOwnerHandlerFactory.class.getName(),
				VERSION);
	}

	public static class ClientConfigurationOwnerHandlerFactory 
	implements ClientInterfaceHandlerFactory<ConfigurationOwner> {
		
		public Class<ConfigurationOwner> interfaceClass() {
			return ConfigurationOwner.class;
		}
		
		public HandlerVersion getVersion() {
			return VERSION;
		}
		
		public ConfigurationOwner createClientHandler(ConfigurationOwner proxy, ClientSideToolkit toolkit) {
			return new ClientCompontOwnerHandler(proxy, toolkit);
		}
	}
	
	/**
	 *  The Client {@link ConfigurationOwner}
	 */
	static class ClientCompontOwnerHandler implements ConfigurationOwner {

		private final ClientSideToolkit clientToolkit;
		
		private final ConfigurationOwnerSupport ownerSupport;
		
		private final SerializableDesignFactory rootDesignFactory;
		
		private final ArooaElement rootElement;
		
		private volatile boolean listening;
		
		private final NotificationListener listener = new NotificationListener() {
			public void handleNotification(Notification notification,
					Object handback) {
				updateSession((ConfigOwnerEvent.Change) notification.getUserData());
			};
		};
		
		ClientCompontOwnerHandler(ConfigurationOwner proxy, final ClientSideToolkit toolkit) {
			this.clientToolkit = toolkit;
			
			ownerSupport = new ConfigurationOwnerSupport(proxy);
			updateSession(null);
			
			ownerSupport.setOnFirst(new Runnable() {
				public void run() {
					updateSession(null);
					toolkit.registerNotificationListener(
							CHANGE_NOTIF_TYPE, listener);
					listening = true;
				}
			});
			
			ownerSupport.setOnEmpty(new Runnable() {
				public void run() {
					listening = false;
					toolkit.removeNotificationListener(
							CHANGE_NOTIF_TYPE, listener);
				}
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
					
				Integer newId = null;
				try {						
					newId = clientToolkit.invoke(SESSION_AVAILABLE);
				}
				catch (InstanceNotFoundException e) {
					// Server Object no longer with us.
					newId = null;
				} 
				catch (Throwable e) {
					throw new UndeclaredThrowableException(e);
				}
					
				if (newId == null) {
					ownerSupport.setConfigurationSession(null);
				}
				else {
					ClientConfigurationSessionHandler existing = 
						(ClientConfigurationSessionHandler)
						ownerSupport.provideConfigurationSession();
					
					if (existing == null || existing.id != newId.intValue()) {
						ownerSupport.setConfigurationSession(null);
						ownerSupport.setConfigurationSession(
								new ClientConfigurationSessionHandler(
										clientToolkit, newId.intValue()));
					}
				}
			}
			else {
				ownerSupport.setConfigurationSession(null);
			}
		}

		public void addOwnerStateListener(OwnerStateListener listener) {
			ownerSupport.addOwnerStateListener(listener);
		}
		
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
		
		private final NotificationListener listener =
			new NotificationListener() {
				public void handleNotification(Notification notification, Object handback) {
					Boolean modified = (Boolean) notification.getUserData();
					if (modified) {
						sessionSupport.modified();
					}
					else {
						sessionSupport.saved();
					}
				}
			};
		
		public ClientConfigurationSessionHandler(final ClientSideToolkit clientToolkit, 
				int id) {
			
			this.id = id;
			this.clientToolkit = clientToolkit;
			sessionSupport = new ConfigurationSessionSupport(this);
			sessionSupport.setOnFirst(new Runnable() {
				public void run() {
					clientToolkit.registerNotificationListener(MODIFIED_NOTIF_TYPE, 
							listener);
				}
			});
			sessionSupport.setOnEmpty(new Runnable() {
				public void run() {
					clientToolkit.removeNotificationListener(MODIFIED_NOTIF_TYPE, 
							listener);
				}
			});
		}
		
		public DragPoint dragPointFor(Object component) {
			
			if (component == null) {
				throw new NullPointerException("No component.");
			}

			try {
				final DragPointInfo dragPointInfo = 
					(DragPointInfo) clientToolkit.invoke(
						DRAG_POINT_INFO, new Object[] { component });
				
				return createDragPoint(component, dragPointInfo);
				
			} catch (Throwable e) {
				throw new UndeclaredThrowableException(e);
			}
		}

		public void save() throws ArooaParseException {
			try {
				clientToolkit.invoke(
						SAVE);
			} catch (Throwable e) {
				throw new UndeclaredThrowableException(e);
			}
		}

		public boolean isModified() {
			try {
				return clientToolkit.invoke(
						IS_MODIFIED);
			} catch (Throwable e) {
				throw new UndeclaredThrowableException(e);
			}
		}
		
		public void addSessionStateListener(SessionStateListener listener) {
			sessionSupport.addSessionStateListener(listener);
		}
		
		public void removeSessionStateListener(SessionStateListener listener) {
			sessionSupport.removeSessionStateListener(listener);
		}
				
		public ArooaDescriptor getArooaDescriptor() {
			return clientToolkit.getClientSession().getArooaSession().getArooaDescriptor();
		}

		private DragPoint createDragPoint(final Object component, final DragPointInfo dragPointInfo) {
			
			if (dragPointInfo == null) {
				return null;
			}
			
			return new DragPoint() {
				public boolean supportsCut() {
					return dragPointInfo.supportsCut;
				}
				
				public boolean supportsPaste() {
					return dragPointInfo.supportsPaste;
				}
				
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
				
				public String copy() {
					return dragPointInfo.copy;
				}
				
				public void cut() {
					try {
						clientToolkit.invoke(
								CUT, new Object[] { component });
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
				}
				public ConfigurationHandle parse(
						ArooaContext parentContext)
				throws ArooaParseException {
					try {
						final XMLConfiguration config = 
							new XMLConfiguration("Server Config", 
									dragPointInfo.copy);

						final ConfigurationHandle handle =
							config.parse(parentContext);

						return new ConfigurationHandle() {
							public ArooaContext getDocumentContext() {
								return handle.getDocumentContext();
							}

							public void save()
							throws ArooaParseException {
								
								config.setSaveHandler(new XMLConfiguration.SaveHandler() {
									@Override
									public void acceptXML(String xml) {
										try {
											if (xml.equals(dragPointInfo.copy)) {
												return;
											}
											
											clientToolkit.invoke(
													REPLACE, new Object[] { component, xml });
										} catch (Throwable e) {
											throw new UndeclaredThrowableException(e);
										}
									}
								});
								
								handle.save();
							}
						};

					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
				}
				public void paste(int index, String config)
				throws ArooaParseException {
					try {
						clientToolkit.invoke(
								PASTE, 
								new Object[] { 
										component,
										index,
										config });
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
				}
			};
		}
		
	}

	class ServerComponentOwnerHandler implements ServerInterfaceHandler {
	
		private final ConfigurationOwner configurationOwner;

		private final ServerSideToolkit toolkit;
		
		private ConfigurationSession configurationSession;
		
		private final SessionStateListener modifiedListener = new SessionStateListener() {

			public void sessionModifed(ConfigSessionEvent event) {
				send(true);
			}
			
			public void sessionSaved(ConfigSessionEvent event) {
				send(false);
			}
			
			void send(final boolean modified) {
				toolkit.runSynchronized(new Runnable() {
					public void run() {
						Notification notification = 
							toolkit.createNotification(MODIFIED_NOTIF_TYPE);
						notification.setUserData(new Boolean(modified));
						toolkit.sendNotification(notification);					
					}
				});
			}
		};

		private final OwnerStateListener configurationListener 
				= new OwnerStateListener() {

			public void sessionChanged(final ConfigOwnerEvent event) {
				configurationSession = configurationOwner.provideConfigurationSession();
				if (configurationSession != null) {
					configurationSession.addSessionStateListener(modifiedListener);
				}
				
				toolkit.runSynchronized(new Runnable() {
					public void run() {
						Notification notification = 
							toolkit.createNotification(CHANGE_NOTIF_TYPE);
						notification.setUserData(event.getChange());
						toolkit.sendNotification(notification);					
					}
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
		
		public Object invoke(RemoteOperation<?> operation, Object[] params) throws MBeanException, ReflectionException {

			if (INFO.equals(operation)) {

				return new ComponentOwnerInfo(configurationOwner);
			}
			
			if (SESSION_AVAILABLE.equals(operation)) {
				if (configurationSession == null) {
					return null;
				}
				else {
					return new Integer(System.identityHashCode(configurationSession));
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
				}
				catch (ArooaParseException e) {
					throw new MBeanException(e);
				}
			}

			if (IS_MODIFIED.equals(operation)) {

				return configurationSession.isModified();
			}
			
			DragPoint dragPoint = null;
			if (params != null && params.length > 0) {
				
				Object component = params[0];
				dragPoint = configurationSession.dragPointFor(component);
			}
			
			if (DRAG_POINT_INFO.equals(operation)) {
				if (dragPoint == null) {
					return null;
				}
				else {
					return new DragPointInfo(dragPoint);
				}
			}
			
			if (dragPoint == null) {
				throw new MBeanException(new IllegalStateException("Null Drag Point - Method " + 
						operation + " should not have been called!"));
			}
			
			if (CUT.equals(operation)) {

				DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
				dragPoint.cut();
				try {
					trn.commit();
				} catch (ArooaParseException e) {
					trn.rollback();
					throw new MBeanException(e);
				}
				
				return null;
			}
			else if (PASTE.equals(operation)) {

				Integer index = (Integer) params[1];
				String config = (String) params[2];
				
				DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
				try {
					dragPoint.paste(index, config);
					trn.commit();					
				}
				catch (Exception e) {
					trn.rollback();
					throw new MBeanException(e);
				}
				return null;
			}
			else if (REPLACE.equals(operation)) {

				String config = (String) params[1];
				
				try {
					XMLArooaParser parser = new XMLArooaParser();
					ConfigurationHandle handle = parser.parse(dragPoint);
					
					ArooaContext documentContext = handle.getDocumentContext();
					
					CutAndPasteSupport.replace(documentContext.getParent(), 
							documentContext, 
							new XMLConfiguration("Edited Config", config));
					handle.save();
				}
				catch (ArooaParseException e) {
					throw new MBeanException(e);
				}
				return null;
			}
			else {
				throw new ReflectionException(
						new IllegalStateException("Invoked for an unknown method [" +
								operation.toString() + "]"), 
								operation.toString());				
			}
			
		}
		
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

class DragPointInfo implements Serializable {
	private static final long serialVersionUID = 2009020400L;
	
	final boolean supportsCut;
	
	final boolean supportsPaste;
	
	final String copy;
                                                            
	DragPointInfo(DragPoint serverDragPoint) {
		this.supportsCut = serverDragPoint.supportsCut();
		this.supportsPaste = serverDragPoint.supportsPaste();
		this.copy = serverDragPoint.copy();
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


