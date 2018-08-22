package org.oddjob.jmx.client;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.oddjob.OjTestCase;

public class SynchronizerTest extends OjTestCase {

	class OurListener implements NotificationListener {

		List<Notification> notifications = 
			new ArrayList<Notification>();
		
		public void handleNotification(Notification notification,
				Object handback) {
			notifications.add(notification);
		}
	}
		
	String type = "X";
	
   @Test
	public void testSynch() {
		
		Notification n0 = new Notification(type, this, 100);
		Notification n1 = new Notification(type, this, 101);
		Notification n2 = new Notification(type, this, 102);
		Notification n3 = new Notification(type, this, 103);
		
		OurListener results = new OurListener();
		
		Synchronizer test = new Synchronizer(results);
		
		test.handleNotification(n0, null);
		test.handleNotification(n1, null);
		test.handleNotification(n3, null);
		
		assertEquals(0, results.notifications.size());
		
		test.synchronize(new Notification[] {
			n1, n2
		});
		
		assertEquals(3, results.notifications.size());
		
		assertEquals(n1, results.notifications.get(0));
		assertEquals(n2, results.notifications.get(1));
		assertEquals(n3, results.notifications.get(2));
		
		test.handleNotification(n3, null);
		
		assertEquals(n3, results.notifications.get(3));
	}
	
}
