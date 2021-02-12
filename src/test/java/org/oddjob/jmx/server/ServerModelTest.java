/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.jmx.server;

import org.junit.Test;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogListener;
import org.oddjob.util.MockThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerModelTest {

    static class LL implements LogListener {
        LogEvent e;

        public void logEvent(LogEvent logEvent) {
            this.e = logEvent;
        }
    }

    public static class LoggingBean implements LogEnabled {
        public String loggerName() {
            return "org.oddjob.test.LoggingBean";
        }
    }

    static class OurRegistry extends MockBeanRegistry {
        @Override
        public String getIdFor(Object component) {
            return "apple";
        }
    }

    /**
     * Test retrieving log events
     */
    @Test
    public void testLogArchiver() {
        LoggingBean bean = new LoggingBean();

        ServerModelImpl sm = new ServerModelImpl(
                new ServerId("//test"),
                new MockThreadManager(),
                new MockServerInterfaceManagerFactory());

        sm.setLogFormat("%m");

        Logger testLogger = LoggerFactory.getLogger(bean.loggerName());
        LoggerAdapter.appenderAdapterFor(bean.loggerName()).setLevel(LogLevel.DEBUG);

        LL ll = new LL();

        ServerContext parentContext = mock(ServerContext.class);
        when(parentContext.getBeanDirectory()).thenReturn(new OurRegistry());

        ServerContext serverContext = new ServerContextImpl(
                bean, sm, parentContext);

        testLogger.info("Test");

        serverContext.getLogArchiver().addLogListener(ll, bean, LogLevel.DEBUG, -1, 10);

        assertThat("event", ll.e, notNullValue());
        assertThat("event message", ll.e.getMessage(), is("Test"));
    }
}
