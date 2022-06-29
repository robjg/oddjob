/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.OurDirs;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoJobTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(EchoJobTest.class);

    @Before
    public void setUp() throws Exception {


        logger.info("------------------  " + getName() + "  -----------------------");
    }

    @Test
    public void testInOddjob1() throws Exception {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <echo id='e'>Hello</echo>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("TEST", xml));

        oj.run();

        Object test = new OddjobLookup(oj).lookup("e");
        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(test));
        assertEquals("Hello", PropertyUtils.getProperty(test, "text"));
    }

    @Test
    public void testInOddjob2() throws Exception {

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/EchoTest2.xml",
                getClass().getClassLoader()));

        oj.run();

        Object test = new OddjobLookup(oj).lookup("2");
        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(test));
        assertEquals("Hello", PropertyUtils.getProperty(test, "text"));
    }

    @Test
    public void testLines() {

        OurDirs dirs = new OurDirs();

        Oddjob oddjob = new Oddjob();
        oddjob.setArgs(new String[]{dirs.base().getPath()});

        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/EchoLinesTest.xml",
                getClass().getClassLoader()));

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();
        }

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        console.dump(logger);

        String[] lines = console.getLines();
        assertEquals(3, lines.length);

        oddjob.destroy();
    }

    @Test
    public void testExample1() {

        Oddjob oddjob = new Oddjob();

        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/EchoExample.xml",
                getClass().getClassLoader()));

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();
        }

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        console.dump(logger);

        String[] lines = console.getLines();

        assertEquals("Hello World", lines[0].trim());

        assertEquals(1, lines.length);

        oddjob.destroy();
    }

    @Test
    public void testExample2() {

        Oddjob oddjob = new Oddjob();

        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/EchoTwice.xml",
                getClass().getClassLoader()));

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();
        }

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        console.dump(logger);

        String[] lines = console.getLines();

        assertEquals("Hello World Twice!", lines[0].trim());
        assertEquals("Hello World Twice!", lines[1].trim());

        assertEquals(2, lines.length);

        oddjob.destroy();
    }
}
