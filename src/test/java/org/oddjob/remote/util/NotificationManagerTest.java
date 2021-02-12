package org.oddjob.remote.util;

import org.junit.Test;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.RemoteException;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class NotificationManagerTest {

    @Test
    public void testSubscribeUnsubscribe() throws RemoteException {

        NotificationType<String> stringType =
                NotificationType.ofName("some.string.event")
                        .andDataType(String.class);

        List<String> subscribed = new ArrayList<>();
        List<String> unSubscribed = new ArrayList<>();

        NotificationManager test = new NotificationManager(
                (remoteId, type) -> subscribed.add("" + remoteId + "-" + type.getName()),
                (remoteId, type) -> unSubscribed.add("" + remoteId + "-" + type.getName()));

        Notification<String> n1 = new Notification<>(1L, stringType, 1000L, "Hello");
        Notification<String> n2 = new Notification<>(1L, stringType, 1001L, "How");
        Notification<String> n3 = new Notification<>(1L, stringType, 1002L, "Are");
        Notification<String> n4 = new Notification<>(1L, stringType, 1003L, "You");

        List<Notification<String>> results1 = new ArrayList<>();

        NotificationListener<String> listener1 = results1::add;

        // Add first listener

        test.addNotificationListener(1L, stringType, listener1);

        assertThat(subscribed.size(), is(1));
        assertThat(subscribed.get(0), is("1-some.string.event"));

        // Send notification 1

        test.handleNotification(n1);

        assertThat(results1.size(), is(1));
        assertThat(results1.get(0), is(n1));

        // Add another listener

        List<Notification<String>> results2 = new ArrayList<>();

        NotificationListener<String> listener2 = results2::add;

        test.addNotificationListener(1L, stringType, listener2);

        assertThat(subscribed.size(), is(1));

        // Send Notification 2

        test.handleNotification(n2);

        assertThat(results1.size(), is(2));
        assertThat(results1.get(1), is(n2));

        assertThat(results2.size(), is(1));
        assertThat(results2.get(0), is(n2));

        // Remove  listener 1

        test.removeNotificationListener(1L, stringType, listener1);

        assertThat(unSubscribed.size(), is(0));

        // Send Notification 3

        test.handleNotification(n3);

        assertThat(results1.size(), is(2));

        assertThat(results2.size(), is(2));
        assertThat(results2.get(1), is(n3));

        // Add Listener 1 back

        test.addNotificationListener(1L, stringType, listener1);

        // Send Notification 4

        test.handleNotification(n4);

        assertThat(results1.size(), is(3));
        assertThat(results2.get(2), is(n4));

        assertThat(results2.size(), is(3));
        assertThat(results2.get(2), is(n4));

        // Remove Both

        test.removeNotificationListener(1L, stringType, listener1);
        test.removeNotificationListener(1L, stringType, listener2);

        assertThat(unSubscribed.size(), is(1));
        assertThat(unSubscribed.get(0), is("1-some.string.event"));
    }

    @Test
    public void testSubscribeUnsubscribeTwoDifferentTypesSameListener() throws RemoteException {

        NotificationType<String> type1 =
                NotificationType.ofName("type1")
                        .andDataType(String.class);

        NotificationType<String> type2 =
                NotificationType.ofName("type2")
                        .andDataType(String.class);

        List<String> subscribed = new ArrayList<>();
        List<String> unSubscribed = new ArrayList<>();

        NotificationManager test = new NotificationManager(
                (remoteId, type) -> subscribed.add("" + remoteId + "-" + type.getName()),
                (remoteId, type) -> unSubscribed.add("" + remoteId + "-" + type.getName()));

        @SuppressWarnings("unchecked")
        NotificationListener<String> notificationListener = mock(NotificationListener.class);

        test.addNotificationListener(42, type1, notificationListener);
        test.addNotificationListener(42, type2, notificationListener);

        assertThat(subscribed.size(), is(2));
        assertThat(subscribed.get(0), is("42-type1"));
        assertThat(subscribed.get(1), is("42-type2"));

        test.removeNotificationListener(42L, type1, notificationListener);
        test.removeNotificationListener(42L, type2, notificationListener);

        assertThat(unSubscribed.size(), is(2));
        assertThat(unSubscribed.get(0), is("42-type1"));
        assertThat(unSubscribed.get(1), is("42-type2"));
    }
}