package org.oddjob.jmx.client;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;

import java.util.ArrayList;
import java.util.List;

public class SynchronizerTest extends OjTestCase {

    static class OurListener implements NotificationListener {

        List<Notification> notifications =
                new ArrayList<>();

        @Override
        public void handleNotification(Notification notification) {
            notifications.add(notification);
        }
    }

    String type = "X";

    @Test
    public void testSync() {

        Notification n0 = new Notification(type, 100, new Object());
        Notification n1 = new Notification(type, 101, new Object());
        Notification n2 = new Notification(type, 102, new Object());
        Notification n3 = new Notification(type, 103, new Object());

        OurListener results = new OurListener();

        Synchronizer test = new Synchronizer(results);

        test.handleNotification(n0);
        test.handleNotification(n1);
        test.handleNotification(n3);

        assertEquals(0, results.notifications.size());

        test.synchronize(new Notification[]{
                n1, n2
        });

        assertEquals(3, results.notifications.size());

        assertEquals(n1, results.notifications.get(0));
        assertEquals(n2, results.notifications.get(1));
        assertEquals(n3, results.notifications.get(2));

        test.handleNotification(n3);

        assertEquals(n3, results.notifications.get(3));
    }

}
