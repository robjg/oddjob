/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.handlers;

import org.junit.Test;
import org.oddjob.Structural;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSession;
import org.oddjob.jmx.client.MockClientSession;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.handlers.StructuralHandlerFactory.ChildData;
import org.oddjob.jmx.server.*;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StructuralHandlerFactoryTest {

    private static class OurServerSideToolkit extends MockServerSideToolkit {

        List<Notification<ChildData>> notifications = new ArrayList<>();

        Map<Long, Object> children = new HashMap<>();

        long objectId = 2L;
        int seq = 0;

        @SuppressWarnings("unchecked")
        @Override
        public void sendNotification(Notification<?> notification) {
            notifications.add((Notification<ChildData>) notification);
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
                    assertThat(child, notNullValue());
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
        public <T> Notification<T> createNotification(NotificationType<T> type,
                                                      T userData) {
            return new Notification<>(objectId, type, seq++, userData);
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

        // Creating handler send notification. Not sure if it should do this
        // but nothings listening
        assertThat(toolkit.notifications.size(), is(1));

        @SuppressWarnings("unchecked")
        Notification<ChildData> last =
                (Notification<ChildData>) handler.invoke(
                        StructuralHandlerFactory.SYNCHRONIZE,
                        new Object[0]);

        assertEquals(1, last.getSequence());
        ChildData lastData0 = last.getData();

        assertEquals(1, lastData0.getRemoteIds().length);
        assertEquals(lastData0.getRemoteIds()[0], 2L);

        Object child = new Object();

        structural.helper.insertChild(1, child);

        assertEquals(2, toolkit.notifications.size());

        // first notification will be ignored.
        Notification<ChildData> n0 = toolkit.notifications.get(0);
        assertEquals(0, n0.getSequence());
        ChildData childData0 = n0.getData();

        assertEquals(1, childData0.getRemoteIds().length);
        assertEquals(lastData0.getRemoteIds()[0], 2L);

        Notification<ChildData> n1 = toolkit.notifications.get(1);
        assertEquals(2, n1.getSequence());
        assertEquals(StructuralHandlerFactory.STRUCTURAL_NOTIF_TYPE,
                n1.getType());

        structural.helper.insertChild(2, new Object());

        assertEquals(3, toolkit.notifications.size());
        Notification<ChildData> n2 = toolkit.notifications.get(2);
        assertEquals(3, n2.getSequence());

        ChildData childData2 = n2.getData();

        assertEquals(3, childData2.getRemoteIds().length);
        assertThat(childData2.getRemoteIds(), is(new long[]{2L, 3L, 4L}));

        structural.helper.removeChildAt(1);

        assertEquals(4, toolkit.notifications.size());
        Notification<ChildData> n3 = toolkit.notifications.get(3);
        assertEquals(4, n3.getSequence());

        ChildData childData3 = n3.getData();

        assertEquals(2, childData3.getRemoteIds().length);
        assertThat(childData3.getRemoteIds(), is(new Long[] {2L, 4L }));

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
                return null;
            }
            else {
                throw new RuntimeException("Unexpected" + remoteOperation);
            }
        }

        @Override
        public <T> void registerNotificationListener(NotificationType<T> eventType,
                                                 NotificationListener<T> notificationListener) {

            if (StructuralHandlerFactory.STRUCTURAL_NOTIF_TYPE.equals(eventType)) {
                if (handler != null) {
                    throw new RuntimeException("Listener not null.");
                }
                this.handler = notificationListener;
            } else {
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

        @Override
        public void childAdded(StructuralEvent event) {
            children.add(event.getIndex(), event.getChild());
        }

        @Override
        public void childRemoved(StructuralEvent event) {
            children.remove(event.getIndex());
        }
    }

    private static class OurStructural implements Structural {
        @Override
        public void addStructuralListener(StructuralListener listener) {
        }

        @Override
        public void removeStructuralListener(StructuralListener listener) {
        }

    }

    @Test
    public void testClientSide() throws NullPointerException {

        ClientInterfaceHandlerFactory<Structural> clientFactory =
                new StructuralHandlerFactory.ClientFactory();

        OurClientToolkit clientToolkit = new OurClientToolkit();

        OurStructural proxy = new OurStructural();

        Structural handler = clientFactory.createClientHandler(
                proxy, clientToolkit);

        ResultListener results = new ResultListener();

        handler.addStructuralListener(results);

        // First

        NotificationType<ChildData> notificationType =
                NotificationType.ofName("ignored")
                        .andDataType(ChildData.class);

        ChildData data1 = new ChildData(new long[]{2L});

        Notification<ChildData> n1 = new Notification<>(1L, notificationType, 0, data1);

        clientToolkit.handler.handleNotification(n1);

        assertEquals(1, results.children.size());

        Object child1 = results.children.get(0);

        assertEquals(clientToolkit.created.get(2L), child1);

        // Second

        ChildData data2 = new ChildData(new long[]{2L, 3L});

        Notification<ChildData> n2 = new Notification<>(1L, notificationType, 0, data2);

        clientToolkit.handler.handleNotification(n2);

        assertEquals(2, results.children.size());

        Object child2 = results.children.get(1);

        assertEquals(clientToolkit.created.get(3L), child2);

        // Third

        ChildData data3 = new ChildData(new long[]{2L, 3L, 4L});
        Notification<ChildData> n3 = new Notification<>(1L, notificationType, 0, data3);

        clientToolkit.handler.handleNotification(n3);

        assertEquals(3, results.children.size());

        Object child3 = results.children.get(2);

        assertEquals(clientToolkit.created.get(
                4L), child3);

        // Fourth

        ChildData data4 = new ChildData(
                        new long[]{3L, 4L});
        Notification<ChildData> n4 = new Notification<>(1L, notificationType, 0, data4);

        clientToolkit.handler.handleNotification(n4);

        assertEquals(2, results.children.size());

        Object child4 = results.children.get(0);

        assertEquals(clientToolkit.created.get(3L), child4);
    }
}
