package org.oddjob.jmx.server;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationType;
import org.oddjob.tools.OddjobTestHelper;

import javax.management.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

public class OddjobMBeanToolkitTest extends OjTestCase {

    private static class OurServerContext extends MockServerContext {
        OurSIMF simf = new OurSIMF();

        @Override
        public ServerModel getModel() {
            return new MockServerModel() {
                @Override
                public ServerInterfaceManagerFactory getInterfaceManagerFactory() {
                    return new ServerInterfaceManagerFactoryImpl(
                            new ServerInterfaceHandlerFactory<?, ?>[]{
                                    simf
                            });
                }
            };
        }
    }

    private interface Gold {

    }

    private static class OurSIMF extends MockServerInterfaceHandlerFactory<Object, Gold> {

        ServerSideToolkit toolkit;

        @Override
        public ServerInterfaceHandler createServerHandler(Object target,
                                                          ServerSideToolkit toolkit) {
            this.toolkit = toolkit;
            return new MockServerInterfaceHandler();
        }

        @Override
        public Class<Object> interfaceClass() {
            return Object.class;
        }

        @Override
        public Class<Gold> clientClass() {
            return Gold.class;
        }

        @Override
        public MBeanAttributeInfo[] getMBeanAttributeInfo() {
            return new MBeanAttributeInfo[0];
        }

        @Override
        public MBeanNotificationInfo[] getMBeanNotificationInfo() {
            return new MBeanNotificationInfo[0];
        }

        @Override
        public MBeanOperationInfo[] getMBeanOperationInfo() {
            return new MBeanOperationInfo[0];
        }
    }

    public static class SomeData implements Serializable {

    }

    @Test
    public void testNotification() throws Exception {

        Object node = new Object();

        OurServerContext context = new OurServerContext();

        MBeanServer mbs = MBeanServerFactory.createMBeanServer();

        OddjobMBean oddjobMBean = new OddjobMBean(node, 0L, null, context);
        ObjectName objectName = oddjobMBean.getObjectName();

        mbs.registerMBean(oddjobMBean, objectName);

        ServerSideToolkit toolkit = context.simf.toolkit;

        List<javax.management.Notification> jmxNotifications = new ArrayList<>();
        mbs.addNotificationListener(objectName,
                (notification, handback) -> jmxNotifications.add(notification),
                null, null);

        NotificationType<SomeData> notificationType =
                NotificationType.ofName("X")
                        .andDataType(SomeData.class);

        Notification n = toolkit.createNotification(notificationType, new SomeData());
        toolkit.sendNotification(n);

        assertThat(jmxNotifications.size(), is(1));
        javax.management.Notification n2 = jmxNotifications.get(0);

        assertEquals(0L, n.getSequence());

        assertThat(n2.getType(), is("X"));
        assertThat(n2.getSequenceNumber(), is(0L));
        assertThat(n2.getUserData(), is(n.getData()));

        // test serializable.
        OddjobTestHelper.copy(n2);

    }
}
