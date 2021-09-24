package org.oddjob.jmx;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobComponentResolver;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.EchoJob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformMBeanServerTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(PlatformMBeanServerTest.class);

    @Before
    public void setUp() throws Exception {
        logger.info("----------------------  " + getName() + "  --------------------");
    }

    @Test
    public void testClientServer() throws Exception {

        ArooaSession arooaSession = new StandardArooaSession();

        Object echo = new OddjobComponentResolver()
                .resolve(new EchoJob(), arooaSession);

        JMXServerJob server = new JMXServerJob();

        server.setRoot(echo);
        server.setArooaSession(arooaSession);
        server.start();

        JMXClientJob client = new JMXClientJob();
        client.setArooaSession(arooaSession);
        client.run();

        Object[] children = OddjobTestHelper.getChildren(client);

        assertEquals(1, children.length);

        client.stop();

        server.stop();
    }

    @Test
    public void testInOddjob() {

        Oddjob server = new Oddjob();
        server.setConfiguration(new XMLConfiguration(
                "org/oddjob/jmx/PlatformMBeanServerExample.xml",
                getClass().getClassLoader()));

        server.run();

        Oddjob client = new Oddjob();
        client.setConfiguration(new XMLConfiguration(
                "org/oddjob/jmx/PlatformMBeanClientExample.xml",
                getClass().getClassLoader()));

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            client.run();
        }

        assertEquals(ParentState.COMPLETE,
                client.lastStateEvent().getState());

        console.dump(logger);

        String[] lines = console.getLines();

        assertEquals("Hello from an Oddjob Server!", lines[0].trim());
        assertEquals(1, lines.length);

        client.destroy();

        server.destroy();
    }

}
