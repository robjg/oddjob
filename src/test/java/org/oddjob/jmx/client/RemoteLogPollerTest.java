/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.*;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.SharedConstants;
import org.oddjob.jmx.server.*;
import org.oddjob.logging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * Test RemoteLogPoller
 */
public class RemoteLogPollerTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(RemoteLogPollerTest.class);

    ClientInterfaceManagerFactory clientInterfaceManagerFactory =
            new ClientInterfaceManagerFactoryBuilder()
                    .addFactories(SharedConstants.DEFAULT_CLIENT_HANDLER_FACTORIES)
                    .build();

    @Before
    public void setUp() {
        logger.debug("================== Running " + getName() + "================");
        System.setProperty("mx4j.log.priority", "trace");
    }

    /**
     * LogListener fixture
     */
    private static class LL implements LogListener {
        String text;

        public void logEvent(LogEvent logEvent) {
            text = logEvent.getMessage();
        }
    }

    private static class OurLogPollable implements LogEnabled, LogPollable {
        long expectedFrom;
        int expectedMax;

        public String loggerName() {
            return ("org.oddjob.TestLogger");
        }

        public String consoleId() {
            return "test";
        }

        public LogEvent[] retrieveConsoleEvents(long from, int max) {
            assertEquals("from", expectedFrom, from);
            assertEquals("max", expectedMax, max);

            return new LogEvent[]{new LogEvent("org.oddjob.TestLogger",
                    0, LogLevel.INFO, "Test Console")};
        }

        public LogEvent[] retrieveLogEvents(long from, int max) {
            assertEquals("from", expectedFrom, from);
            assertEquals("max", expectedMax, max);

            return new LogEvent[]{new LogEvent("org.oddjob.TestLogger",
                    0, LogLevel.INFO, "Test Log")};
        }

        public String url() {
            return "//test/";
        }
    }

    /**
     * Test polling using a mock LogPollable.
     */
    @Test
    public void testPoll() {
        OurLogPollable pollable = new OurLogPollable();
        RemoteLogPoller test = new RemoteLogPoller(pollable, 10, 10);

        pollable.expectedFrom = -1;
        pollable.expectedMax = 10;

        LL consoleListener = new LL();
        LL logListener = new LL();

        test.addConsoleListener(consoleListener, pollable, -1, 0);
        test.addLogListener(logListener, pollable, LogLevel.INFO, -1, 0);


        assertEquals("Console event", "Test Console", consoleListener.text);

        assertEquals("Log event", "Test Log", logListener.text);
    }

    /**
     * Fixture class that has a logger property.
     */
    private static class LogThing implements LogEnabled {

        public String loggerName() {
            return "foo";
        }

    }

    /**
     * Fixture class that does not have a log property.
     */
    private static class NoLogThing {
    }

    /**
     * Fixture that implements the Archiver methods
     */
    private static class MockArchivers implements LogArchiver, ConsoleArchiver {
        public void addLogListener(LogListener l, Object component, LogLevel level, long from, int max) {
            l.logEvent(new LogEvent("foo", 0, LogLevel.INFO, "Hello"));
        }

        public void removeLogListener(LogListener l, Object component) {
        }

        public void addConsoleListener(LogListener l, Object compoennt, long from, int max) {
            l.logEvent(new LogEvent("console", 0, LogLevel.INFO, "Goodbye"));
        }

        public void removeConsoleListener(LogListener l, Object component) {
        }

        public String consoleIdFor(Object component) {
            return "console";
        }
    }

    private static class OurHierarchicalRegistry extends MockBeanRegistry {

        @Override
        public String getIdFor(Object component) {
            assertNotNull(component);
            return "x";
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
     * A ServerContext that returns our archivers
     */
    private static class MyServerContext extends MockServerContext {
        ServerInterfaceManagerFactory simf;

        MockArchivers archivers = new MockArchivers();

        public ConsoleArchiver getConsoleArchiver() {
            return archivers;
        }

        public LogArchiver getLogArchiver() {
            return archivers;
        }

        @Override
        public ServerModel getModel() {
            return new MockServerModel() {
                @Override
                public ServerInterfaceManagerFactory getInterfaceManagerFactory() {
                    return simf;
                }
            };
        }

        @Override
        public BeanDirectory getBeanDirectory() {
            return new OurHierarchicalRegistry();
        }

        @Override
        public Address getAddress() {
            return null;
        }

        @Override
        public ServerId getServerId() {
            return new ServerId("//test");
        }
    }

    private static class OurServerSession extends MockServerSession {

        ArooaSession session = new StandardArooaSession();

        @Override
        public ArooaSession getArooaSession() {
            return session;
        }
    }

    /**
     * Full client server test of logging.
     *
     * @throws Exception
     */
    @Test
    public void testLoggingUsingMBean() throws Exception {
        // set up interfaces for MBean
        LogThing component = new LogThing();

        ServerInterfaceManagerFactoryImpl imf =
                new ServerInterfaceManagerFactoryImpl();

        MyServerContext serverContext = new MyServerContext();
        serverContext.simf = imf;

        OddjobMBean mb = new OddjobMBean(
                component, 0,
                new OurServerSession(),
                serverContext);

        // create an MBean
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();

        long objectId = 2L;
        ObjectName on = OddjobMBeanFactory.objectName(objectId);
        mbs.registerMBean(mb, on);

        ClientSession clientSession = new ClientSessionImpl(
                mbs,
                new DummyNotificationProcessor(),
                clientInterfaceManagerFactory,
                new OurArooaSession(),
                logger);

        Object proxy = clientSession.create(objectId);

        // check client side logger
        assertTrue(proxy instanceof LogEnabled);
        assertEquals("foo", LogHelper.getLogger(proxy));

        assertEquals("console", ((LogPollable) proxy).consoleId());

        // create a remote log poller
        RemoteLogPoller poller = new RemoteLogPoller(
                proxy, 10, 10);

        LL cl = new LL();
        LL ll = new LL();

        poller.addConsoleListener(cl, proxy, -1, 100);
        poller.addLogListener(ll, proxy, LogLevel.DEBUG, -1, 5);

        poller.poll();

        assertEquals("Hello", ll.text);
        assertEquals("Goodbye", cl.text);

    }

    /**
     * Full client server test of not logging.
     *
     * @throws Exception
     */
    @Test
    public void testNotLoggingUsingMBean() throws Exception {
        NoLogThing component = new NoLogThing();

        ServerInterfaceManagerFactoryImpl imf =
                new ServerInterfaceManagerFactoryImpl();

        MyServerContext serverContext = new MyServerContext();
        serverContext.simf = imf;

        OddjobMBean mb = new OddjobMBean(
                component, 0L,
                new OurServerSession(),
                serverContext);


        MBeanServer mbs = MBeanServerFactory.createMBeanServer();

        long objectId = 2L;
        ObjectName on = OddjobMBeanFactory.objectName(objectId);
        mbs.registerMBean(mb, on);

        ClientSession clientSession = new ClientSessionImpl(
                mbs,
                new DummyNotificationProcessor(),
                clientInterfaceManagerFactory,
                new OurArooaSession(),
                logger);

        Object proxy = clientSession.create(objectId);

        assertTrue(proxy instanceof LogEnabled);
        assertNull(LogHelper.getLogger(proxy));

        assertEquals("console", ((LogPollable) proxy).consoleId());

        RemoteLogPoller poller = new RemoteLogPoller(
                proxy, 10, 10);

        LL cl = new LL();
        LL ll = new LL();

        poller.addConsoleListener(cl, proxy, -1, 100);
        poller.addLogListener(ll, proxy, LogLevel.DEBUG, -1, 5);

        poller.poll();

        assertEquals("No Log available", ll.text);
        assertEquals("Goodbye", cl.text);

    }
}
