/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.jmx.server;

import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.MockStateful;
import org.oddjob.OjTestCase;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.SharedConstants;
import org.oddjob.jmx.handlers.StatefulHandlerFactory;
import org.oddjob.jmx.handlers.StructuralHandlerFactory;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogEvent;
import org.oddjob.remote.Notification;
import org.oddjob.remote.RemoteException;
import org.oddjob.state.JobState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.util.MockThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for an OddjobMBeanTest.
 */
public class OddjobMBeanTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(OddjobMBeanTest.class);

    private ServerModel sm;

    private int unique;

    private class OurHierarchicalRegistry extends MockBeanRegistry {

        @Override
        public String getIdFor(Object component) {
            assertNotNull(component);
            return "x" + unique++;
        }

    }

    @Before
    public void setUp() {
        logger.debug("==================== Running {} =====================", getName());

        ServerInterfaceManagerFactoryImpl imf =
                new ServerInterfaceManagerFactoryImpl();

        imf.addServerHandlerFactories(
                new ResourceFactoryProvider(
                        new StandardArooaSession()).getHandlerFactories());

        sm = new ServerModelImpl(
                new ServerId("//test"),
                new MockThreadManager(),
                imf);
    }

    private static class OurServerSession extends MockServerSession {

        ArooaSession session = new StandardArooaSession();

        @Override
        public ArooaSession getArooaSession() {
            return session;
        }
    }

    /**
     * Test creating and registering an OddjobMBean.
     */
    @Test
    public void testRegister()
            throws Exception {
        Runnable myJob = () -> {

        };

        BeanRegistry beanRegistry = new OurHierarchicalRegistry();
        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(beanRegistry);

        ServerContext serverContext = new ServerContextImpl(
                myJob, sm, parentContext);

        OddjobMBean ojmb = OddjobMBean.create(
                myJob, 0L,
                new OurServerSession(),
                serverContext);

        ObjectName on = new ObjectName("Oddjob:name=whatever");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        mbs.registerMBean(ojmb, on);

        assertTrue(mbs.isRegistered(on));

        mbs.unregisterMBean(on);
    }

    /**
     * Test notifying job state to a NotificationListener.
     */
    @Test
    public void testNotifyState()
            throws Exception {
        /* Fixture Stateful */
        class MyStateful extends MockStateful {
            StateListener jsl;

            public void addStateListener(StateListener listener) {
                jsl = listener;
                listener.jobStateChange(StateEvent.now(this, JobState.READY));
            }

            public void removeStateListener(StateListener listener) {
            }

            public void foo() {
                jsl.jobStateChange(StateEvent.now(this, JobState.COMPLETE));
            }
        }
        MyStateful myJob = new MyStateful();

        MyNotLis myNotLis = new MyNotLis();

        BeanRegistry beanRegistry = new OurHierarchicalRegistry();
        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(beanRegistry);

        ServerContext serverContext = new ServerContextImpl(
                myJob, sm, parentContext);

        // create and register MBean.
        OddjobMBean ojmb = OddjobMBean.create(
                myJob,
                0L,
                new OurServerSession(),
                serverContext);

        ObjectName on = ojmb.getObjectName();

        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        mbs.registerMBean(ojmb, on);

        MBeanInfo mBeanInfo = mbs.getMBeanInfo(on);
        MBeanNotificationInfo[] notificationInfo = mBeanInfo.getNotifications();
        assertThat(notificationInfo.length, is(1));
        assertThat(notificationInfo[0].getNotifTypes()[0],
                is(StatefulHandlerFactory.STATE_CHANGE_NOTIF_TYPE.getName()));


        // add notification listener.
        mbs.addNotificationListener(on, myNotLis, null, null);

        // check null state to begin with
        assertEquals("number", 0, myNotLis.getNum());

        // change state.
        myJob.foo();

        //check state
        assertEquals("number", 1, myNotLis.getNum());
        assertEquals("source", on, myNotLis.getNotification(0).getSource());
        assertEquals("type", StatefulHandlerFactory.STATE_CHANGE_NOTIF_TYPE.getName(),
                myNotLis.getNotification(0).getType());

        mbs.unregisterMBean(on);
    }

    /**
     * Test notification of structural change.
     */
    @Test
    public void testNotifyStructure()
            throws JMException, RemoteException {
        final Object myChild = new Object() {
            public String toString() {
                return "my child";
            }
        };

        class MyStructural implements Structural {
            StructuralListener jsl;
            boolean foo = false;

            public void addStructuralListener(StructuralListener listener) {
                jsl = listener;
            }

            public void removeStructuralListener(StructuralListener listener) {
            }

            public void foo() {
                if (foo) {
                    jsl.childRemoved(new StructuralEvent(this, myChild, 0));
                } else {
                    jsl.childAdded(new StructuralEvent(this, myChild, 0));
                }
                foo = !foo;
            }
        }
        MyStructural myJob = new MyStructural();

        MyNotLis myNotLis = new MyNotLis();

        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        OddjobMBeanFactory f = new OddjobMBeanFactory(mbs,
                new StandardArooaSession());

        BeanRegistry beanRegistry = new OurHierarchicalRegistry();
        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(beanRegistry);

        ServerContext serverContext = new ServerContextImpl(
                myJob, sm, parentContext);

        long objectId = f.createMBeanFor(myJob, serverContext);
        ObjectName on = OddjobMBeanFactory.objectName(objectId);

        mbs.addNotificationListener(on,
                myNotLis, null, null);

        @SuppressWarnings("unchecked") Notification<StructuralHandlerFactory.ChildData> lastStructuralNotification =
                (Notification<StructuralHandlerFactory.ChildData>) mbs.invoke(on, "structuralSynchronize",
                        new Object[0], new String[0]);
        MatcherAssert.assertThat( lastStructuralNotification, Matchers.notNullValue());

        myJob.foo();
        assertEquals("notifications", 1, myNotLis.getNum());
        assertEquals("source", on, myNotLis.getNotification(0).getSource());
        assertEquals("type", StructuralHandlerFactory.STRUCTURAL_NOTIF_TYPE.getName(),
                myNotLis.getNotification(0).getType());

        myJob.foo();
        assertEquals("notifications", 2, myNotLis.getNum());
        assertEquals("source", on, myNotLis.getNotification(1).getSource());
        assertEquals("type", StructuralHandlerFactory.STRUCTURAL_NOTIF_TYPE.getName(),
                myNotLis.getNotification(1).getType());

        mbs.unregisterMBean(on);
    }

    public static class MyBean {
        public String getFruit() {
            return "apple";
        }
    }

    /**
     * Test DynaClass of a RemoteBean.
     */
    @Test
    public void testGetDynaClass() throws Exception {
        MyBean sampleBean = new MyBean();

        BeanRegistry beanRegistry = new OurHierarchicalRegistry();
        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(beanRegistry);

        ServerContext serverContext = new ServerContextImpl(
                sampleBean, sm, parentContext);

        OddjobMBean test = OddjobMBean.create(
                sampleBean, 0L,
                new OurServerSession(),
                serverContext);

        DynaClass dc = (DynaClass) test.invoke(
                "getDynaClass", new Object[]{}, new String[]{});

        assertNotNull(dc);
        DynaProperty dp = dc.getDynaProperty("fruit");
        assertEquals(String.class, dp.getType());
    }

    public static class LoggingBean implements LogEnabled {
        public String loggerName() {
            return "org.oddjob.test.LoggingBean";
        }
    }

    /**
     * Test retrieving log events
     */
    @Test
    public void testLogging() throws Exception {
        LoggingBean bean = new LoggingBean();

        ((ServerModelImpl) sm).setLogFormat("%m");

        BeanRegistry beanRegistry = new OurHierarchicalRegistry();
        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(beanRegistry);

        ServerContext serverContext = new ServerContextImpl(
                bean, sm, parentContext);

        OddjobMBean ojmb = OddjobMBean.create(
                bean, 0L,
                new OurServerSession(),
                serverContext);

        LoggerAdapter.appenderAdapterFor(bean.loggerName()).setLevel(LogLevel.DEBUG);
        Logger testLogger = LoggerFactory.getLogger(bean.loggerName());
        testLogger.info("Test");

        LogEvent[] events = (LogEvent[]) ojmb.invoke(SharedConstants.RETRIEVE_LOG_EVENTS_METHOD,
                new Object[]{
                        (long) -1,
                        10},
                new String[]{
                        Long.TYPE.getName(), Integer.TYPE.getName()});

        assertEquals("num events", 1, events.length);
        assertEquals("event 0", "Test", events[0].getMessage());
    }
}

/**
 * Fixture listener.
 */
class MyNotLis implements javax.management.NotificationListener {
    private static final Logger logger = LoggerFactory.getLogger(MyNotLis.class);

    private static class Pair {
        private final javax.management.Notification notification;
        private final Object handback;

        Pair(javax.management.Notification notification, Object handback) {
            this.notification = notification;
            this.handback = handback;
        }
    }

    private final List<Pair> notifications = new ArrayList<>();

    public void handleNotification(javax.management.Notification arg0, Object arg1) {
        // check notifications serializable.
        try {
            if (!(arg0.getSource() instanceof ObjectName)) {
                throw new ClassCastException("Doh!");
            }
            javax.management.Notification copy = OddjobTestHelper.copy(arg0);
            if (!(copy.getSource() instanceof ObjectName)) {
                throw new ClassCastException("Doh!");
            }
        } catch (Exception e) {
            logger.error("Notification Listener failed.", e);
            throw new RuntimeException(e);
        }
        Pair p = new Pair(arg0, arg1);
        notifications.add(p);
    }

    int getNum() {
        return notifications.size();
    }

    javax.management.Notification getNotification(int i) {
        Pair p = notifications.get(i);
        return p.notification;
    }

    Object getHandback(int i) {
        Pair p = notifications.get(i);
        return p.handback;
    }
}


