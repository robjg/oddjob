package org.oddjob.jmx.handlers;

import javax.management.Notification;
import javax.management.NotificationListener;

import junit.framework.TestCase;

import org.oddjob.Iconic;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.IconTip;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ServerInterfaceHandler;

public class IconicHandlerFactoryTest extends TestCase {

	class OurIconic implements Iconic {
		
		IconHelper helper = new IconHelper(this);

		{
			helper.changeIcon(IconHelper.EXECUTING);
		}
		
		public void addIconListener(IconListener listener) {
			helper.addIconListener(listener);
		}
		
		public void removeIconListener(IconListener listener) {
			helper.removeIconListener(listener);
		}
		
		public IconTip iconForId(String iconId) {
			return helper.iconForId(iconId);
		}
	}
	
	class OurClientToolkit extends MockClientSideToolkit {
		ServerInterfaceHandler server;

		@SuppressWarnings("unchecked")
		@Override
		public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
				throws Throwable {
			return (T) server.invoke(remoteOperation, args);
		}
	}

	class OurServerToolkit extends MockServerSideToolkit {
		long seq = 0;
		
		@Override
		public void runSynchronized(Runnable runnable) {
			runnable.run();
		}
		
		@Override
		public Notification createNotification(String type) {
			return new Notification(type, this, seq++);
		}
		
		@Override
		public void sendNotification(Notification notification) {
		}
	}
	
	public void testClientIconFor() {
		OurIconic iconic = new OurIconic();
		
		IconicHandlerFactory test = new IconicHandlerFactory();
		
		OurServerToolkit serverToolkit = new OurServerToolkit();
		
		ServerInterfaceHandler serverHandler = test.createServerHandler(
				iconic, serverToolkit);
		
		OurClientToolkit toolkit = new OurClientToolkit();
		toolkit.server = serverHandler;
		
		Iconic h = 
			new IconicHandlerFactory.ClientIconicHandlerFactory(
					).createClientHandler(iconic, toolkit);

		IconTip result = h.iconForId(IconHelper.EXECUTING);
		
		assertEquals("Executing", result.getToolTip());
	}
	
	
	class OurClientToolkit2 extends MockClientSideToolkit {
		ServerInterfaceHandler server;
		NotificationListener listener;
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
				throws Throwable {
			return (T) server.invoke(remoteOperation, args);
		}
		
		
		public void registerNotificationListener(String eventType, NotificationListener notificationListener) {
			if (listener != null) {
				throw new RuntimeException("Only one listener expected.");
			}
			assertEquals(IconicHandlerFactory.ICON_CHANGED_NOTIF_TYPE, eventType);
			
			this.listener = notificationListener;
		}
		
		@Override
		public void removeNotificationListener(String eventType,
				NotificationListener notificationListener) {
			if (listener == null) {
				throw new RuntimeException("Only one listener remove expected.");
			}
			
			assertEquals(IconicHandlerFactory.ICON_CHANGED_NOTIF_TYPE, eventType);
			assertEquals(this.listener, notificationListener);
			
			this.listener = null;
		}
	}

	class OurServerToolkit2 extends MockServerSideToolkit {
		long seq = 0;
		
		NotificationListener listener;
		
		@Override
		public void runSynchronized(Runnable runnable) {
			runnable.run();
		}
		
		@Override
		public Notification createNotification(String type) {
			return new Notification(type, this, seq++);
		}
		
		@Override
		public void sendNotification(Notification notification) {
			if (listener != null) {
				listener.handleNotification(notification, null);
			}
		}
	}

	class Result implements IconListener {
		
		IconEvent event;
		
		public void iconEvent(IconEvent e) {
			event = e;
		}
	}
	
	public void testIconListeners() {
		OurIconic iconic = new OurIconic();
		
		IconicHandlerFactory test = new IconicHandlerFactory();
		
		OurServerToolkit2 serverToolkit = new OurServerToolkit2();
		
		ServerInterfaceHandler serverHandler = test.createServerHandler(
				iconic, serverToolkit);
		
		OurClientToolkit2 clientToolkit = new OurClientToolkit2();
		clientToolkit.server = serverHandler;
		
		Iconic local = new IconicHandlerFactory.ClientIconicHandlerFactory(
				).createClientHandler(iconic, clientToolkit);

		Result result = new Result();
		
		local.addIconListener(result);
		
		assertEquals(IconHelper.EXECUTING, result.event.getIconId());
		
		Result result2 = new Result();
		
		local.addIconListener(result2);
		
		assertEquals(IconHelper.EXECUTING, result2.event.getIconId());
		
		serverToolkit.listener = clientToolkit.listener;
		
		iconic.helper.changeIcon(IconHelper.COMPLETE);
		
		assertEquals(IconHelper.COMPLETE, result.event.getIconId());
		assertEquals(IconHelper.COMPLETE, result2.event.getIconId());
		
		local.removeIconListener(result2);
		
		assertNotNull(clientToolkit.listener);
		
		local.removeIconListener(result);
		
		assertNull(clientToolkit.listener);
		
	}
	
}
