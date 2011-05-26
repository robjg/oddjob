/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.jmx.handlers;

import javax.management.Notification;
import javax.management.NotificationListener;

import junit.framework.TestCase;

import org.oddjob.MockStateful;
import org.oddjob.Stateful;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

public class StatefulHandlerFactoryTest extends TestCase {

	class OurStateful extends MockStateful {
		JobStateListener l;
		public void addJobStateListener(JobStateListener listener) {
			assertNull(l);
			l = listener;
			l.jobStateChange(new JobStateEvent(this, JobState.READY));
		}
		public void removeJobStateListener(JobStateListener listener) {
			assertNotNull(l);
			l = null;
		}		
	}
	
	class OurClientToolkit extends MockClientSideToolkit {
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
			assertEquals(StatefulHandlerFactory.STATE_CHANGE_NOTIF_TYPE, eventType);
			
			this.listener = notificationListener;
		}
		
		@Override
		public void removeNotificationListener(String eventType,
				NotificationListener notificationListener) {
			if (listener == null) {
				throw new RuntimeException("Only one listener remove expected.");
			}
			
			assertEquals(StatefulHandlerFactory.STATE_CHANGE_NOTIF_TYPE, eventType);
			assertEquals(this.listener, notificationListener);
			
			this.listener = null;
		}
	}

	class OurServerSideToolkit extends MockServerSideToolkit {

		long seq = 0;
		
		NotificationListener listener;
		
		public void runSynchronized(Runnable runnable) {
			runnable.run();
		}
		
		@Override
		public Notification createNotification(String type) {
			return new Notification(type, this, seq++);
		}
		
		public void sendNotification(Notification notification) {
			if (listener != null) {
				listener.handleNotification(notification, null);
			}
		}
				
	}
	
	class Result implements JobStateListener {
		JobStateEvent event;
		
		public void jobStateChange(JobStateEvent event) {
			this.event = event;
		}
	}
	
	public void testAddRemoveListener() throws Exception {
		
		StatefulHandlerFactory test = new StatefulHandlerFactory();
		
		assertEquals(1, test.getMBeanNotificationInfo().length);
		
		OurStateful stateful = new OurStateful(); 
		OurServerSideToolkit serverToolkit = new OurServerSideToolkit();

		// create the handler
		ServerInterfaceHandler serverHandler = test.createServerHandler(
				stateful, serverToolkit);

		// which should add a listener to our stateful
		assertNotNull("listener added.", stateful.l);

		OurClientToolkit clientToolkit = new OurClientToolkit();

		Stateful local = new StatefulHandlerFactory.ClientStatefulHandlerFactory(
				).createClientHandler(new MockStateful(), clientToolkit);
		
		clientToolkit.server = serverHandler;

		Result result = new Result();
		
		local.addJobStateListener(result);
		
		assertEquals("State ready", JobState.READY, 
				result.event.getJobState());

		Result result2 = new Result();
		
		local.addJobStateListener(result2);
		
		assertEquals("State ready", JobState.READY, 
				result2.event.getJobState());

		serverToolkit.listener = clientToolkit.listener;
		
		stateful.l.jobStateChange(new JobStateEvent(stateful, JobState.COMPLETE));

		// check the notification is sent
		assertEquals("State complete", JobState.COMPLETE, 
				result.event.getJobState());
		assertEquals("State complete", JobState.COMPLETE, 
				result2.event.getJobState());
		
		local.removeJobStateListener(result);
		
		assertNotNull(clientToolkit.listener);
		
		local.removeJobStateListener(result2);
		
		assertNull(clientToolkit.listener);
		
	}
	
	
}
