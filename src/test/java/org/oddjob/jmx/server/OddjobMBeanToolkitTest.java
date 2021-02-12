package org.oddjob.jmx.server;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationType;
import org.oddjob.tools.OddjobTestHelper;

import javax.management.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OddjobMBeanToolkitTest {

    private interface Gold {

    }

    public static class SomeData implements Serializable {

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNotification() throws Exception {

        NotificationType<SomeData> notificationType =
                NotificationType.ofName("X")
                        .andDataType(SomeData.class);

        ServerInterfaceHandler handler = mock(ServerInterfaceHandler.class);

        ServerInterfaceHandlerFactory<Object, Gold> serverInterfaceHandlerFactory =
                mock(ServerInterfaceHandlerFactory.class);
        when(serverInterfaceHandlerFactory.serverClass()).thenReturn(Object.class);
        when(serverInterfaceHandlerFactory.getMBeanAttributeInfo())
                .thenReturn(new MBeanAttributeInfo[0]);
        when(serverInterfaceHandlerFactory.getMBeanOperationInfo())
                .thenReturn(new MBeanOperationInfo[0]);
        when(serverInterfaceHandlerFactory.getNotificationTypes())
                .thenReturn(Collections.singletonList(notificationType));
        when(serverInterfaceHandlerFactory
                .createServerHandler(any(Object.class), any(ServerSideToolkit.class)))
                .thenReturn(handler);

        ServerModel serverModel = mock(ServerModel.class);
        when(serverModel.getInterfaceManagerFactory()).thenReturn(
                new ServerInterfaceManagerFactoryImpl(new ServerInterfaceHandlerFactory<?, ?>[]{
                        serverInterfaceHandlerFactory}));

        ServerContext serverContext = mock(ServerContext.class);
        when(serverContext.getModel()).thenReturn(serverModel);

        Object node = new Object();

        MBeanServer mbs = MBeanServerFactory.createMBeanServer();

        OddjobMBean oddjobMBean = new OddjobMBean(node, 0L, null, serverContext);
        ObjectName objectName = oddjobMBean.getObjectName();

        mbs.registerMBean(oddjobMBean, objectName);

        ArgumentCaptor<ServerSideToolkit> captor = ArgumentCaptor.forClass(ServerSideToolkit.class);

        verify(serverInterfaceHandlerFactory, times(1))
                .createServerHandler(any(), captor.capture());

        ServerSideToolkit toolkit = captor.getValue();

        List<javax.management.Notification> jmxNotifications = new ArrayList<>();
        mbs.addNotificationListener(objectName,
                (notification, handback) -> jmxNotifications.add(notification),
                null, null);

        Notification<SomeData> n = toolkit.createNotification(notificationType, new SomeData());
        toolkit.sendNotification(n);

        assertThat(jmxNotifications.size(), is(1));
        javax.management.Notification n2 = jmxNotifications.get(0);

        assertThat(n.getSequence(), is(0L));

        assertThat(n2.getType(), is("X"));
        assertThat(n2.getSequenceNumber(), is(0L));
        assertThat(n2.getUserData(), is(n.getData()));

        // test serializable.
        OddjobTestHelper.copy(n2);
    }
}
