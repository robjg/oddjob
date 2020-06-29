/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.jmx.client;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.OddjobConsole;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.*;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.Path;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.handlers.*;
import org.oddjob.jmx.server.*;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogEvent;
import org.oddjob.util.MockThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Test a ClientNode.
 */
public class ClientNodeTest extends OjTestCase {
    public static final Logger logger = LoggerFactory.getLogger(ClientNodeTest.class);

    /**
     * Fixture interface which contains the minimum
     * set of operations a server side MBean must implement.
     */
    public interface OJMBeanInternals extends RemoteOddjobBean {
        String toString();
    }

    int unique;

    /**
     * Fixture for the base class of an MBean which provides
     * a minimal implementation of an OddjobMBean.
     */
    public abstract class BaseMockOJMBean extends NotificationBroadcasterSupport
            implements OJMBeanInternals {

        int instance = unique++;

        protected final Set<ClientHandlerResolver<?>> handlerFactories =
                new HashSet<>();

        {
            handlerFactories.add(new RemoteOddjobHandlerFactory().clientHandlerFactory());
            handlerFactories.add(new ObjectInterfaceHandlerFactory().clientHandlerFactory());
        }

        public ServerInfo serverInfo() {
            return new ServerInfo(
                    new Address(new ServerId(url()), new Path(id())),
                    handlerFactories.toArray(
                            new ClientHandlerResolver[0])
            );
        }

        public void noop() {
        }

        protected String url() {
            return "//test";
        }

        protected String id() {
            return "x" + instance;
        }

        public String toString() {
            return "MockOJMBean.";
        }

        public String loggerName() {
            return "org.oddjob.TestLogger";
        }
    }

    ///////////////////// simplest bean possible /////////////////

    public interface SimpleMBean extends OJMBeanInternals {
    }

    public class Simple extends BaseMockOJMBean
            implements SimpleMBean {
        public String toString() {
            return "test";
        }
    }

    private static class OurArooaSession extends MockArooaSession {
        @Override
        public ArooaDescriptor getArooaDescriptor() {
            return new MockArooaDescriptor() {
                @Override
                public ClassResolver getClassResolver() {
                    return new MockClassResolver() {
                        @Override
                        public Class<?> findClass(String className) {
                            try {
                                return Class.forName(className);
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                }
            };
        }
    }


    /**
     * Test the client can create a proxy for the simplest bean
     * possible.
     *
     * @throws Exception
     */
    @Test
    public void testSimple() throws Exception {
        Simple mb = new Simple();

        MBeanServer mbs = MBeanServerFactory.createMBeanServer();

        long beanId = 2L;
        ObjectName on = OddjobMBeanFactory.objectName(beanId);
        mbs.registerMBean(mb, on);

        ClientSessionImpl clientSession = new ClientSessionImpl(
                mbs,
                new DummyNotificationProcessor(),
                new OurArooaSession(),
                logger);

        Object proxy = clientSession.create(beanId);

        assertEquals("test", proxy.toString());
    }

    /**
     * Test the same proxy instances are equal
     *
     * @throws Exception
     */
    @Test
    public void testEquals() throws Exception {
        Simple mb = new Simple();

        MBeanServer mbs = MBeanServerFactory.createMBeanServer();

        long objectId = 2L;
        ObjectName on = OddjobMBeanFactory.objectName(objectId);
        mbs.registerMBean(mb, on);

        ClientSessionImpl clientSession = new ClientSessionImpl(
                mbs,
                new DummyNotificationProcessor(),
                new OurArooaSession(),
                logger);

        Object proxy = clientSession.create(objectId);

        assertEquals(proxy, proxy);
        assertEquals(proxy.hashCode(), proxy.hashCode());
    }

    //////////////////////// interfaces ////////////////////////////

    public interface MockRunnableMBean extends Runnable, OJMBeanInternals {
    }

    public class MockRunnable extends BaseMockOJMBean
            implements MockRunnableMBean {
        boolean ran;

        public MockRunnable() {
            handlerFactories.add(new RunnableHandlerFactory().clientHandlerFactory());
        }

        public void run() {
            ran = true;
        }
    }

    @Before
    public void setUp() {
        logger.debug("================== Running " + getName() + "================");
        System.setProperty("mx4j.log.priority", "trace");
    }

    /**
     * Test running a proxy runs it's server side equivelant.
     *
     * @throws Exception
     */
    @Test
    public void testRunnable()
            throws Exception {
        MockRunnable mb = new MockRunnable();

        MBeanServer mbs = MBeanServerFactory.createMBeanServer();

        long objectId = 2L;
        ObjectName on = OddjobMBeanFactory.objectName(objectId);
        mbs.registerMBean(mb, on);

        ClientSessionImpl clientSession = new ClientSessionImpl(
                mbs,
                new DummyNotificationProcessor(),
                new OurArooaSession(),
                logger);

        Object proxy = clientSession.create(objectId);

        assertTrue("Runnable", proxy instanceof Runnable);

        ((Runnable) proxy).run();

        assertTrue("Ran", mb.ran);
    }

    ///////////////// MBean getter setter tests //////////////////

    // a bean with a property.
    public static class Fred implements Serializable {
        private static final long serialVersionUID = 20051117;

        public String getFruit() {
            return "apples";
        }
    }

    // a dyna class with one property but supports adding any.
    public static class MyDC implements DynaClass, Serializable {
        private static final long serialVersionUID = 20051117;

        public DynaProperty[] getDynaProperties() {
            return new DynaProperty[]{
                    new DynaProperty("fred", Fred.class),
                    //	new DynaProperty("description", Map.class)
            };
        }

        public DynaProperty getDynaProperty(String arg0) {
            return new DynaProperty(arg0);
        }

        public String getName() {
            return "MyDynaClass";
        }

        public DynaBean newInstance() {
            throw new UnsupportedOperationException("newInstance");
        }
    }

    public static class Bean implements DynaBean, LogEnabled {
        public boolean contains(String name, String key) {
            logger.debug("contains(" + name + ", " + key + ")");
            return false;
        }

        public Object get(String name) {
            logger.debug("get(" + name + ")");
            if ("fred".equals(name)) {
                return new Fred();
            } else if ("description".equals(name)) {
                Map<Object, Object> m = new HashMap<>();
                m.put("fooled", "you");
                return m;
            }
            return null;
        }

        public Object get(String name, int index) {
            logger.debug("get(" + name + ", " + index + ")");
            return null;
        }

        public Object get(String name, String key) {
            logger.debug("get(" + name + ", " + key + ")");
            return null;
        }

        public DynaClass getDynaClass() {
            logger.debug("getDynaClass");
            return new MyDC();
        }

        public void remove(String name, String key) {
            logger.debug("remove(" + name + ", " + key + ")");
        }

        public void set(String name, int index, Object value) {
            logger.debug("set(" + name + ", " + index + ", " + value + ")");
        }

        public void set(String name, Object value) {
            logger.debug("set(" + name + ", " + value + ")");
        }

        public void set(String name, String key, Object value) {
            logger.debug("set(" + name + ", " + key + ", " + value + ")");
        }

        public String loggerName() {
            return "org.oddjob.TestLogger";
        }
    }

    public static class MyDynamicMBean extends NotificationBroadcasterSupport
            implements DynamicMBean {

        public Object getAttribute(String attribute) {
            logger.debug("MyDynamicMBean getting attribute [" + attribute + "]");
            throw new UnsupportedOperationException();
        }

        public AttributeList getAttributes(String[] attributes) {
            return null;
        }

        public MBeanInfo getMBeanInfo() {
            return new MBeanInfo(this.getClass().getName(),
                    "Test MBean",
                    new MBeanAttributeInfo[0],
                    new MBeanConstructorInfo[0],
                    new MBeanOperationInfo[0],
                    new MBeanNotificationInfo[0]);
        }

        public Object invoke(String actionName, Object[] arguments, String[] signature) throws MBeanException, ReflectionException {
            logger.debug("MyDynamicMBean invoking [" + actionName + "]");
            if ("toString".equals(actionName)) {
                return "MyDynamicMBean";
            } else if ("serverInfo".equals(actionName)) {
                return new ServerInfo(
                        new Address(new ServerId("//foo/"), new Path("whatever")),
                        new ClientHandlerResolver[]{
                                new ObjectInterfaceHandlerFactory().clientHandlerFactory(),
                                new RemoteOddjobHandlerFactory().clientHandlerFactory(),
                                new DynaBeanHandlerFactory().clientHandlerFactory()}
                );
            } else if ("loggerName".equals(actionName)) {
                return "org.oddjob.TestLogger";
            } else if ("getDynaClass".equals(actionName)) {
                return new MyDC();
            } else if ("get".equals(actionName)) {
                return new Fred();
            } else {
                throw new MBeanException(
                        new UnsupportedOperationException("Unsupported Method [" + actionName + "]"));
            }
        }

        public void setAttribute(Attribute attribute) {
            throw new UnsupportedOperationException();
        }

        public AttributeList setAttributes(AttributeList attributes) {
            throw new UnsupportedOperationException();
        }

    }

    // this test uses a dyna bean get
    @Test
    public void testSimpleGet() throws Exception {
        MyDynamicMBean firstBean = new MyDynamicMBean();

        MBeanServer mbs = MBeanServerFactory.createMBeanServer();

        long objectId = 2L;
        ObjectName on = OddjobMBeanFactory.objectName(objectId);
        mbs.registerMBean(firstBean, on);

        ClientSessionImpl clientSession = new ClientSessionImpl(
                mbs,
                new DummyNotificationProcessor(),
                new OurArooaSession(),
                logger);

        Object proxy = clientSession.create(objectId);

        assertNotNull(proxy);

        BeanUtilsPropertyAccessor propertyAccessor = new BeanUtilsPropertyAccessor();

        String fruit = (String) propertyAccessor.getProperty(proxy, "fred.fruit");
        assertEquals("apples", fruit);

        // test a component registry nested lookup
        // need to add a new level to create the new registry
        MyDynamicMBean nestedBean = new MyDynamicMBean();

        ObjectName on2 = new ObjectName("oddjob:name=whatever2");
        mbs.registerMBean(nestedBean, on2);
    }

    private static class OurHierarchicalRegistry extends MockBeanRegistry {

        @Override
        public String getIdFor(Object component) {
            assertNotNull(component);
            return "x";
        }
    }

    private static class OurServerSession extends MockServerSession {

        ArooaSession session = new StandardArooaSession();

        @Override
        public ArooaSession getArooaSession() {
            return session;
        }
    }

    // and this test uses an OddjobMBean get.
    @Test
    public void testBean() throws Exception {

        try (OddjobConsole.Close close = OddjobConsole.initialise()) {

            Object o = new Bean();
            ServerModel sm = new ServerModelImpl(
                    new ServerId("//whatever"),
                    new MockThreadManager(),
                    new ServerInterfaceManagerFactoryImpl()
            );

            ServerContext srvcon = new ServerContextImpl(o, sm,
                    new OurHierarchicalRegistry());

            Object mb = new OddjobMBean(o, OddjobMBeanFactory.objectName(0),
                    new OurServerSession(), srvcon);

            MBeanServer mbs = MBeanServerFactory.createMBeanServer();

            long objectId = 2L;
            ObjectName on = OddjobMBeanFactory.objectName(objectId);
            mbs.registerMBean(mb, on);

            ClientSessionImpl clientSession = new ClientSessionImpl(
                    mbs,
                    new DummyNotificationProcessor(),
                    new OurArooaSession(),
                    logger);

            Object proxy = clientSession.create(objectId);

            assertNotNull(proxy);

            ArooaSession session = new StandardArooaSession();
            Map<String, String> map = new UniversalDescriber(
                    session).describe(proxy);
            assertNotNull(map);

            BeanUtilsPropertyAccessor bubh = new BeanUtilsPropertyAccessor();

            Object gotten = bubh.getProperty(proxy, "fred.fruit");
            assertEquals("apples", gotten);
        }
    }

    ////// Logging Test

    public interface MockLoggingMBean extends OJMBeanInternals, LogPollable {
    }

    public class MockLogging extends BaseMockOJMBean
            implements MockLoggingMBean {
        public MockLogging() {
            handlerFactories.add(new LogPollableHandlerFactory().clientHandlerFactory());
        }

        public LogEvent[] retrieveLogEvents(long from, int max) {
            return new LogEvent[]{
                    new LogEvent("foo", 0, LogLevel.DEBUG, "Test")};
        }

        public LogEvent[] retrieveConsoleEvents(long from, int max) {
            throw new RuntimeException("Unexpected.");
        }

        public String consoleId() {
            return "foo";
        }

        public String url() {
            return super.url();
        }
    }

    /**
     * Test retrieving log events.
     *
     * @throws Exception
     */
    @Test
    public void testLogging()
            throws Exception {
        MockLogging mb = new MockLogging();

        MBeanServer mbs = MBeanServerFactory.createMBeanServer();

        long objectId = 2L;
        ObjectName on = OddjobMBeanFactory.objectName(objectId);
        mbs.registerMBean(mb, on);

        beanDump(mbs, on);

        ClientSessionImpl clientSession = new ClientSessionImpl(
                mbs,
                new DummyNotificationProcessor(),
                new OurArooaSession(),
                logger);

        Object proxy = clientSession.create(objectId);

        assertTrue("Log Pollable", proxy instanceof LogPollable);
        LogPollable test = (LogPollable) proxy;

        assertEquals("url", "//test", test.url());
        LogEvent[] events = test.retrieveLogEvents(-1L, 10);

        assertEquals("num events", 1, events.length);
        assertEquals("event", "Test", events[0].getMessage());

    }

    ///////////////////////////////////////////

    static void beanDump(MBeanServer mbs, ObjectName on)
            throws ReflectionException, InstanceNotFoundException, IntrospectionException {
        MBeanInfo info = mbs.getMBeanInfo(on);
        MBeanOperationInfo[] opInfo = info.getOperations();
        for (MBeanOperationInfo mBeanOperationInfo : opInfo) {
            logger.debug("Op: " + mBeanOperationInfo.getName());
        }
        MBeanAttributeInfo[] atInfo = info.getAttributes();
        for (MBeanAttributeInfo mBeanAttributeInfo : atInfo) {
            logger.debug("At: " + mBeanAttributeInfo.getName());
        }
    }


}
