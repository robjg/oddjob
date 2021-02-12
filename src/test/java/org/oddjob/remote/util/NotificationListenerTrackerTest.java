package org.oddjob.remote.util;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.RemoteException;
import org.oddjob.remote.RemoteNotifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class NotificationListenerTrackerTest {

    @Test
    public void testAddAndRemoveAllSameGroupSameRemoteDifferentType() throws RemoteException {

        NotificationType<String> type1 =
                NotificationType.ofName("some.string.event")
                        .andDataType(String.class);

        NotificationType<Integer> type2 =
                NotificationType.ofName("some.integer.event")
                        .andDataType(Integer.class);

        RemoteNotifier remoteNotifier = mock(RemoteNotifier.class);

        NotificationListenerTracker<String> tracker = new NotificationListenerTracker<>(remoteNotifier);

        @SuppressWarnings("unchecked")
        NotificationListener<String> listener1 = mock(NotificationListener.class);

        @SuppressWarnings("unchecked")
        NotificationListener<Integer> listener2 = mock(NotificationListener.class);

        tracker.addNotificationListener("foo", 1000L, type1, listener1);

        verify(remoteNotifier, times(1))
                .addNotificationListener(eq(1000L), eq(type1), ArgumentMatchers.same(listener1));

        tracker.addNotificationListener("foo", 1000L, type2, listener2);

        verify(remoteNotifier, times(1))
                .addNotificationListener(eq(1000L), eq(type2), ArgumentMatchers.same(listener2));

        assertThat(tracker.removeAll("bar"), is(false));

        assertThat(tracker.removeAll("foo"), is(true));

        verify(remoteNotifier, times(1))
                .removeNotificationListener(eq(1000L), eq(type1), ArgumentMatchers.same(listener1));

        verify(remoteNotifier, times(1))
                .removeNotificationListener(eq(1000L), eq(type2), ArgumentMatchers.same(listener2));

        verifyNoMoreInteractions(remoteNotifier);
    }

    @Test
    public void testAddAndRemoveAllDifferentGroupDifferentRemoteSameType() throws RemoteException {

        NotificationType<String> type1 =
                NotificationType.ofName("some.string.event")
                        .andDataType(String.class);

        RemoteNotifier remoteNotifier = mock(RemoteNotifier.class);

        NotificationListenerTracker<String> tracker = new NotificationListenerTracker<>(remoteNotifier);

        @SuppressWarnings("unchecked")
        NotificationListener<String> listener1 = mock(NotificationListener.class);

        tracker.addNotificationListener("foo", 1000L, type1, listener1);

        verify(remoteNotifier, times(1))
                .addNotificationListener(eq(1000L), eq(type1), ArgumentMatchers.same(listener1));

        tracker.addNotificationListener("bar", 1001L, type1, listener1);

        verify(remoteNotifier, times(1))
                .addNotificationListener(eq(1001L), eq(type1), ArgumentMatchers.same(listener1));

        assertThat(tracker.removeAll("foo"), is(true));

        verify(remoteNotifier, times(1))
                .removeNotificationListener(eq(1000L), eq(type1), ArgumentMatchers.same(listener1));

        tracker.removeNotificationListener("bar", 1001L, type1);

        verify(remoteNotifier, times(1))
                .removeNotificationListener(eq(1001L), eq(type1), ArgumentMatchers.same(listener1));

        assertThat(tracker.removeAll("bar"), is(false));

        verifyNoMoreInteractions(remoteNotifier);
    }

}