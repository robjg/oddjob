/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.jmx.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.oddjob.OjTestCase;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.remote.Implementation;
import org.oddjob.remote.NotificationType;

import javax.management.*;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class InterfaceManagerImplTest extends OjTestCase {

    public static final HandlerVersion VERSION = new HandlerVersion(2, 0);

    interface MockI {

    }

    static class MockInterfaceInfo implements ServerInterfaceHandlerFactory<MockI, MockI> {
        boolean destroyed;

        @Override
        public HandlerVersion getHandlerVersion() {
            return VERSION;
        }

        @Override
        public ServerInterfaceHandler createServerHandler(MockI target, ServerSideToolkit ojmb) {
            return new ServerInterfaceHandler() {
                public void destroy() {
                    destroyed = true;
                }

                public Object invoke(RemoteOperation<?> operation, Object[] params) {
                    if ("foo".equals(operation.getActionName())) {
                        return "Apples";
                    } else if ("moo".equals(operation.getActionName())) {
                        return "Oranges";
                    } else throw new RuntimeException("Unexpected!");
                }

            };
        }

        public MBeanAttributeInfo[] getMBeanAttributeInfo() {
            return new MBeanAttributeInfo[0];
        }

        @Override
        public List<NotificationType<?>> getNotificationTypes() {
            return Collections.emptyList();
        }

        @Override
        public MBeanOperationInfo[] getMBeanOperationInfo() {
            return new MBeanOperationInfo[]{
                    new MBeanOperationInfo(
                            "foo",
                            "Foo method",
                            new MBeanParameterInfo[0],
                            String.class.getName(),
                            MBeanOperationInfo.INFO),
                    new MBeanOperationInfo(
                            "moo",
                            "Moo method",
                            new MBeanParameterInfo[0],
                            String.class.getName(),
                            MBeanOperationInfo.ACTION_INFO)};
        }

        @Override
        public Class<MockI> serverClass() {
            return MockI.class;
        }

        @Override
        public Class<MockI> clientClass() {
            return MockI.class;
        }
    }

    @Mock
    ServerSideToolkit serverSideToolkit;


    @Test
    public void testAllClientInfo() {
        MockI target = new MockI() {
        };

        ServerInterfaceManager test = new ServerInterfaceManagerImpl(
                target, serverSideToolkit, new ServerInterfaceHandlerFactory[]{new MockInterfaceInfo()});

        Implementation<?>[] result = test.allClientInfo();

        assertEquals(1, result.length);
    }

    @Test
    public void testAllClientInfoReadOnly() {
        MockI target = new MockI() {
        };

        ServerInterfaceManager test = new ServerInterfaceManagerImpl(
                target, serverSideToolkit,
                new ServerInterfaceHandlerFactory[]{new MockInterfaceInfo()},
                opInfo -> opInfo.getImpact() == MBeanOperationInfo.INFO);

        Implementation<?>[] result = test.allClientInfo();

        assertEquals(0, result.length);
    }

    @Test
    public void testInvoke() throws MBeanException, ReflectionException {
        MockI target = new MockI() {
        };

        ServerInterfaceManager test = new ServerInterfaceManagerImpl(
                target, serverSideToolkit, new ServerInterfaceHandlerFactory[]{new MockInterfaceInfo()});

        Object result = test.invoke(
                "foo",
                new Object[0],
                new String[0]);

        assertEquals("Apples", result);

        result = test.invoke(
                "moo",
                new Object[0],
                new String[0]);

        assertEquals("Oranges", result);
    }

    @Test
    public void testInvokeWithAccessController() throws MBeanException, ReflectionException {
        MockI target = new MockI() {
        };

        ServerInterfaceManager test = new ServerInterfaceManagerImpl(
                target, serverSideToolkit, new ServerInterfaceHandlerFactory[]{new MockInterfaceInfo()},
                opInfo -> opInfo.getImpact() == MBeanOperationInfo.INFO);

        Object result = test.invoke(
                "foo",
                new Object[0],
                new String[0]);

        assertEquals("Apples", result);

        try {
            test.invoke(
                    "moo",
                    new Object[0],
                    new String[0]);
            fail("Moo should fail!");
        } catch (SecurityException e) {
            // expected
        }
    }

    @Test
    public void testDestory() {
        MockI target = new MockI() {
        };

        MockInterfaceInfo factory =
                new MockInterfaceInfo();

        ServerInterfaceManager test = new ServerInterfaceManagerImpl(
                target, serverSideToolkit, new ServerInterfaceHandlerFactory[]{factory});

        test.destroy();

        assertTrue(factory.destroyed);
    }
}
