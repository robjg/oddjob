/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.handlers;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.Structural;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSession;
import org.oddjob.jmx.client.MockClientSession;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.*;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructuralHandlerFactoryTest extends OjTestCase {

	int unique;
	
	private static class OurServerSideToolkit extends MockServerSideToolkit {
		List<Notification> notifications = new ArrayList<>();

		Map<Long, Object> children = new HashMap<>();
		
		long objectId = 2L;
		int seq = 0;
		
		@Override
		public void sendNotification(Notification notification) {
			notifications.add(notification);
		}
		
		@Override
		public void runSynchronized(Runnable runnable) {
			runnable.run();
		}
		
		@Override
		public ServerSession getServerSession() {
			return new MockServerSession() {
				@Override
				public long createMBeanFor(Object child,
						ServerContext childContext) {
					try {
						children.put(objectId, child);
						return objectId++;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				@Override
				public void destroy(long childName) {
					Object child = children.remove(childName);
					assertNotNull(child);
				}
			};
		}

		@Override
		public ServerContext getContext() {
			return new MockServerContext() {
				@Override
				public ServerContext addChild(Object child) {
					return new MockServerContext();
				}
			};
		}
		
		@Override
		public Notification createNotification(String type, Object userData) {
			return new Notification(type, seq++, userData);
		}
	}
	
	private static class MyStructural implements Structural {
		ChildHelper<Object> helper = new ChildHelper<>(this);
		public void addStructuralListener(StructuralListener listener) {
			helper.addStructuralListener(listener);
		}
		public void removeStructuralListener(StructuralListener listener) {
			helper.removeStructuralListener(listener);
		}
	}
	
   @Test
	public void testServerSide() throws MBeanException, ReflectionException, NullPointerException {
		MyStructural structural = new MyStructural();
		structural.helper.insertChild(0, new Object());
		
		OurServerSideToolkit toolkit = new OurServerSideToolkit();
		
		StructuralHandlerFactory test = new StructuralHandlerFactory();
		ServerInterfaceHandler handler = test.createServerHandler(structural, toolkit);

		
		assertEquals(1, toolkit.notifications.size());	
		
		Notification[] last = 
			(Notification[]) handler.invoke(
					StructuralHandlerFactory.SYNCHRONIZE, 
					new Object[0]);
		assertEquals(1, last.length);
		Notification last0 = last[0];
		assertEquals(1, last0.getSequenceNumber());
		StructuralHandlerFactory.ChildData lastData0 = 
			(StructuralHandlerFactory.ChildData) last0.getUserData();
		
		
		assertEquals(1, lastData0.getChildObjectNames().length);
		assertEquals(lastData0.getChildObjectNames()[0], new Long(2L));

		Object child = new Object();
		
		structural.helper.insertChild(1, child);
		
		assertEquals(2, toolkit.notifications.size());

		// first notification will be ignored.
		Notification n0 = toolkit.notifications.get(0);
		assertEquals(0, n0.getSequenceNumber());
		StructuralHandlerFactory.ChildData childData0 = 
			(StructuralHandlerFactory.ChildData) n0.getUserData();
		
		assertEquals(1, childData0.getChildObjectNames().length);
		assertEquals(lastData0.getChildObjectNames()[0], new Long(2L));
		
		Notification n1 = toolkit.notifications.get(1);
		assertEquals(2, n1.getSequenceNumber());
		assertEquals(StructuralHandlerFactory.STRUCTURAL_NOTIF_TYPE, 
				n1.getType());
		
		structural.helper.insertChild(2, new Object());
		
		assertEquals(3, toolkit.notifications.size());
		Notification n2 = toolkit.notifications.get(2);
		assertEquals(3, n2.getSequenceNumber());		
		
		StructuralHandlerFactory.ChildData childData2 = 
			(StructuralHandlerFactory.ChildData) n2.getUserData();
		
		assertEquals(3, childData2.getChildObjectNames().length);
		assertEquals(childData2.getChildObjectNames()[0], new Long(2L));
		assertEquals(childData2.getChildObjectNames()[1], new Long(3L));
		assertEquals(childData2.getChildObjectNames()[2], new Long(4L));
		
		structural.helper.removeChildAt(1);
		
		assertEquals(4, toolkit.notifications.size());
		Notification n3 = toolkit.notifications.get(3);
		assertEquals(4, n3.getSequenceNumber());
		
		StructuralHandlerFactory.ChildData childData3 = 
			(StructuralHandlerFactory.ChildData) n3.getUserData();
		
		assertEquals(2, childData3.getChildObjectNames().length);
		assertEquals(childData3.getChildObjectNames()[0], new Long(2L));
		assertEquals(childData3.getChildObjectNames()[1], new Long(4L));
		
		handler.destroy();
		
		assertTrue(structural.helper.isNoListeners());
	}
	
	private static class OurClientToolkit extends MockClientSideToolkit {
		
		NotificationListener handler;
		
		Map<Long, Object> created =
			new HashMap<>();
		Map<Object, Long> toNames =
			new HashMap<>();
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
				throws Throwable {
			if (StructuralHandlerFactory.SYNCHRONIZE.equals(remoteOperation)) {
				return (T) new Notification[0];
			}
			return null;
		}
		
		public void registerNotificationListener(String eventType,
				NotificationListener notificationListener) {

			if (StructuralHandlerFactory.STRUCTURAL_NOTIF_TYPE.equals(eventType)) {
				if (handler != null) {
					throw new RuntimeException("Listener not null.");
				}
				this.handler= notificationListener;
			}
			else {
				throw new RuntimeException("Unexpected.");
			}
		}
		
		@Override
		public ClientSession getClientSession() {
			return new MockClientSession() {
				@Override
				public Object create(long objectName) {
					Object child = new Object();
					created.put(objectName, child);
					toNames.put(child, objectName);
					return child;
				}
				
				@Override
				public void destroy(Object proxy) {
					long objectName = toNames.remove(proxy);
					created.remove(objectName);
				}
			};
		}
	}
	
	private static class ResultListener implements StructuralListener {
		
		List<Object> children = new ArrayList<>();
		
		public void childAdded(StructuralEvent event) {
			children.add(event.getIndex(), event.getChild());
		}
		
		public void childRemoved(StructuralEvent event) {
			children.remove(event.getIndex());
		}
	}
	
	private static class OurStructural implements Structural {
		public void addStructuralListener(StructuralListener listener) {
		}
		public void removeStructuralListener(StructuralListener listener) {
		}
		
	}
	
   @Test
	public void testClientSide() throws NullPointerException {
		
		ClientInterfaceHandlerFactory<Structural> clientFactory = 
			new StructuralHandlerFactory.ClientStructuralHandlerFactory();
		
		OurClientToolkit clientToolkit = new OurClientToolkit();
		
		OurStructural proxy = new OurStructural();
		
		Structural handler = clientFactory.createClientHandler(
				proxy, clientToolkit);
		
		ResultListener results = new ResultListener();
		
		handler.addStructuralListener(results);
		
		// First
		
		StructuralHandlerFactory.ChildData data1 = 
			new StructuralHandlerFactory.ChildData(
				new Long[] { 2L });

		Notification n1 = new Notification("ignored",0, data1);
		
		clientToolkit.handler.handleNotification(n1);
		
		assertEquals(1, results.children.size());
		
		Object child1 = results.children.get(0);
		
		assertEquals(clientToolkit.created.get(2L), child1);

		// Second
		
		StructuralHandlerFactory.ChildData data2 = 
			new StructuralHandlerFactory.ChildData(
				new Long[] { 2L, 3L });
		Notification n2 = new Notification("ignored", 0, data2);
		
		clientToolkit.handler.handleNotification(n2);
		
		assertEquals(2, results.children.size());
		
		Object child2 = results.children.get(1);
		
		assertEquals(clientToolkit.created.get(3L), child2);
		
		// Third
		
		StructuralHandlerFactory.ChildData data3 = 
			new StructuralHandlerFactory.ChildData(
				new Long[] { 2L, 3L, 4L });
		Notification n3 = new Notification("ignored", 0, data3);
		
		clientToolkit.handler.handleNotification(n3);
		
		assertEquals(3, results.children.size());
		
		Object child3 = results.children.get(2);
		
		assertEquals(clientToolkit.created.get(
				4L), child3);
		
		// Fourth
		
		StructuralHandlerFactory.ChildData data4 = 
			new StructuralHandlerFactory.ChildData(
				new Long[] { 3L, 4L });
		Notification n4 = new Notification("ignored", 0, data4);
		
		clientToolkit.handler.handleNotification(n4);
		
		assertEquals(2, results.children.size());
		
		Object child4 = results.children.get(0);
		
		assertEquals(clientToolkit.created.get(3L), child4);
	}
}
