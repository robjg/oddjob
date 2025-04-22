/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob;

import org.junit.Test;
import org.oddjob.arooa.ArooaException;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.types.XMLConfigurationType;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.input.InputHandler;
import org.oddjob.jobs.EchoJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Rob Gordon.
 */
public class OddjobTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(OddjobTest.class);

    /**
     * Test resetting Oddjob
     */
    @Test
    public void testReset() {
        class MyStructuralListener implements StructuralListener {
            int count;

            public void childAdded(StructuralEvent event) {
                count++;
            }

            public void childRemoved(StructuralEvent event) {
                count--;
            }
        }

        String xml =
                "<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
                        " <job>" +
                        "  <state:flag id='flag' state='COMPLETE'/>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("XML", xml));

        StateSteps ojSteps = new StateSteps(oj);

        ojSteps.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.COMPLETE);

        MyStructuralListener childListener = new MyStructuralListener();
        oj.addStructuralListener(childListener);
        assertEquals(0, childListener.count);

        oj.run();

        assertEquals(1, childListener.count);

        ojSteps.checkNow();

        Stateful flag = (Stateful) new OddjobLookup(oj).lookup("flag");

        StateSteps flagSteps = new StateSteps(flag);
        flagSteps.startCheck(JobState.COMPLETE);

        flagSteps.checkNow();

        flagSteps.startCheck(JobState.COMPLETE, JobState.READY,
                JobState.DESTROYED);

        ojSteps.startCheck(ParentState.COMPLETE, ParentState.READY);

        oj.hardReset();

        flagSteps.checkNow();
        ojSteps.checkNow();

        ojSteps.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.COMPLETE);

        oj.run();

        ojSteps.checkNow();

        ojSteps.startCheck(ParentState.COMPLETE, ParentState.READY);

        oj.hardReset();

        ojSteps.checkNow();

        ojSteps.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.COMPLETE);

        oj.run();

        ojSteps.checkNow();

        ojSteps.startCheck(ParentState.COMPLETE, ParentState.DESTROYED);

        oj.destroy();
    }

    /**
     * Test reseting Oddjob
     */
    @Test
    public void testSoftReset() {
        class MyL implements StructuralListener {
            int count;

            public void childAdded(StructuralEvent event) {
                count++;
            }

            public void childRemoved(StructuralEvent event) {
                count--;
            }
        }

        String xml =
                "<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
                        " <job>" +
                        "  <state:flag state='INCOMPLETE'/>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("XML", xml));

        MyL l = new MyL();
        oj.addStructuralListener(l);

        assertEquals(0, l.count);

        oj.run();

        assertEquals(1, l.count);
        assertEquals(ParentState.INCOMPLETE, oj.lastStateEvent().getState());

        oj.softReset();

        assertEquals(1, l.count);
        assertEquals(ParentState.READY, oj.lastStateEvent().getState());

        oj.run();

        assertEquals(1, l.count);
        assertEquals(ParentState.INCOMPLETE, oj.lastStateEvent().getState());

        oj.softReset();

        assertEquals(1, l.count);
        assertEquals(ParentState.READY, oj.lastStateEvent().getState());

        oj.run();

        assertEquals(1, l.count);
        assertEquals(ParentState.INCOMPLETE, oj.lastStateEvent().getState());

        oj.destroy();
    }

    /**
     * Test Oddjob can be rerun after a configuration
     * failure using a soft reset.
     */
    @Test
    public void testSoftResetOnFailure() {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <idontexist/>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("XML", xml));

        oj.run();

        assertEquals(ParentState.EXCEPTION, oj.lastStateEvent().getState());

        String xml2 =
                "<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
                        " <job>" +
                        "  <state:flag state='COMPLETE'/>" +
                        " </job>" +
                        "</oddjob>";

        oj.setConfiguration(new XMLConfiguration("XML", xml2));
        oj.softReset();

        assertEquals(ParentState.READY, oj.lastStateEvent().getState());

        oj.run();

        assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

        oj.destroy();
    }

    /**
     * Test loading an Oddjob without a child.
     *
     */
    @Test
    public void testLoadNoChild() {
        String config = "<oddjob id='this'/>";

        Oddjob test = new Oddjob();
        test.setConfiguration(new XMLConfiguration("XML", config));
        test.run();
        Object root = new OddjobLookup(test).lookup("this");

        assertEquals(Oddjob.OddjobRoot.class, root.getClass());

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

        test.hardReset();

        test.run();

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

        test.destroy();
    }

    @Test
    public void testLoadOddjobClassloader() {
        String config =
                "<oddjob>" +
                        " <job>" +
                        "  <oddjob id='nested'>" +
                        "   <classLoader>" +
                        "    <url-class-loader>" +
                        "     <files>" +
                        "      <files files='build/*.jar'/>" +
                        "     </files>" +
                        "    </url-class-loader>" +
                        "   </classLoader>" +
                        "  </oddjob>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("XML", config));

        oj.load();

        String nestedConf = "<oddjob/>";
        Oddjob test = (Oddjob) new OddjobLookup(oj).lookup("nested");
        oj.setConfiguration(new XMLConfiguration("TEST", nestedConf));
        test.run();

        assertNotNull("Nested classloader",
                test.getClassLoader());

        oj.destroy();
    }


    /**
     * Test a nested lookup.
     *
     */
    @Test
    public void testLookup() throws Exception {

        String config =
                "<oddjob id='this'>" +
                        " <job>" +
                        "  <oddjob id='nested'>" +
                        "   <configuration>" +
                        "    <value value='${inner-config}'/>" +
                        "   </configuration>" +
                        "  </oddjob>" +
                        " </job>" +
                        "</oddjob>";

        String nested =
                "<oddjob>" +
                        " <job>" +
                        "  <variables id='fruits'>" +
                        "   <fruit>" +
                        "    <value value='apple'/>" +
                        "   </fruit>" +
                        "  </variables>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oj = new Oddjob();

        oj.setConfiguration(new XMLConfiguration("XML", config));

        XMLConfigurationType configType = new XMLConfigurationType();
        configType.setXml(nested);

        oj.setExport("inner-config", configType);
        oj.run();

        String fruit = new OddjobLookup(oj).lookup(
                "nested/fruits.fruit", String.class);

        assertEquals("apple", fruit);

        oj.destroy();
    }

    @Test
    public void testArgs() throws Exception {

        String config =
                "<oddjob id='this'>" +
                        " <job>" +
                        "  <variables id='fruits'>" +
                        "	<fruit>" +
                        "    <value value='${this.args[0]}'/>" +
                        "	</fruit>" +
                        "  </variables>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob test = new Oddjob();
        test.setConfiguration(new XMLConfiguration("XML", config));

        // Test without arg.

        test.run();

        assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(test);

        String fruit = lookup.lookup("fruits.fruit", String.class);
        assertEquals(null, fruit);

        test.hardReset();

        // And with arg.

        test.setArgs(new String[]{"apple"});

        test.run();

        fruit = lookup.lookup("fruits.fruit", String.class);
        assertEquals("apple", fruit);

        test.destroy();
    }

    private static class OurInputHandler implements InputHandler {

        @Override
        public Session start() {

            return requests -> {
                Properties properties = new Properties();
                properties.setProperty("our.file.name", "Fruit.txt");
                return properties;
            };
        }
    }

    @Test
    public void testOptionalArgumentExample() {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/OptionalFileNameArg.xml",
                getClass().getClassLoader()));
        oddjob.setInputHandler(new OurInputHandler());

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();

            oddjob.hardReset();
            oddjob.setArgs(new String[]{"Pizzas.txt"});

            oddjob.run();
        }

        console.dump(logger);

        String[] lines = console.getLines();

        assertEquals("File Name is Fruit.txt", lines[0].trim());
        assertEquals("File Name is Pizzas.txt", lines[1].trim());
        assertEquals(2, lines.length);

        oddjob.destroy();
    }

    @Test
    public void testInheritedProperties() throws Exception {

        String config =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <properties>" +
                        "     <values>" +
                        "      <value key='fruit' value='apple'/>" +
                        "     </values>" +
                        "    </properties>" +
                        "    <oddjob id='nested'>" +
                        "     <configuration>" +
                        "      <arooa:configuration xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
                        "       <xml>" +
                        "        <xml>" +
                        "<oddjob>" +
                        " <job>" +
                        "  <variables id='fruits'>" +
                        "	<fruit>" +
                        "    <value value='${fruit}'/>" +
                        "	</fruit>" +
                        "  </variables>" +
                        " </job>" +
                        "</oddjob>" +
                        "        </xml>" +
                        "       </xml>" +
                        "      </arooa:configuration>" +
                        "     </configuration>" +
                        "    </oddjob>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";


        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("XML", config));
        oj.run();

        String fruit = new OddjobLookup(oj).lookup(
                "nested/fruits.fruit", String.class);
        assertEquals("apple", fruit);

        oj.destroy();
    }

    /**
     * Test Oddjob dir property.
     */
    @Test
    public void testDir() throws IOException {
        Path workDir = OurDirs.workPathDir(getClass().getSimpleName(), true);

        File testFile = workDir.resolve("xyz.xml").toFile();

        Oddjob oj = new Oddjob();
        oj.setFile(testFile);

        assertEquals(workDir.toFile().getAbsoluteFile(), oj.getDir());

        oj.destroy();
    }

    /**
     * Test Component Registry management. Test Oddjob
     * adds and clears up it's children OK.
     *
     * @throws ArooaParseException If parsing fails.
     */
    @Test
    public void testRegistryManagement() throws ArooaParseException {

        final ArooaSession session = new OddjobSessionFactory().createSession();

        class OurContext extends MockArooaContext {
            @Override
            public ArooaSession getSession() {
                return session;
            }

            @Override
            public RuntimeConfiguration getRuntime() {
                return new MockRuntimeConfiguration() {
                    @Override
                    public void configure() throws ArooaException {
                    }
                };
            }
        }

        Oddjob oj = new Oddjob();
        oj.setArooaSession(session);

        ComponentPool pool = session.getComponentPool();

        pool.registerComponent(
                new ComponentTrinity(
                        oj, oj, new OurContext())
                , "oj");

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <echo id='echo'>Hello</echo>" +
                        " </job>" +
                        "</oddjob>";

        oj.setConfiguration(new XMLConfiguration("XML", xml));

        BeanDirectory dir = session.getBeanRegistry();

        assertEquals(null, dir.lookup("oj/echo"));

        oj.run();

        assertNotNull(dir.lookup("oj/echo"));

        oj.hardReset();

        assertEquals(null, dir.lookup("oj/echo"));

        oj.run();

        assertNotNull(dir.lookup("oj/echo"));

        oj.destroy();
    }

    @Test
    public void testCreateFile() throws IOException {

        File testFile = OurDirs.workPathDir(getClass().getSimpleName(), true)
                .resolve("OddjobCreateFileTest.xml")
                .toFile();

        EchoJob echo = new EchoJob();
        echo.setOutput(new FileOutputStream(testFile));
        echo.setText("<oddjob/>");

        echo.run();

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <oddjob file='" + testFile.getAbsolutePath() + "'/>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

        //noinspection ResultOfMethodCallIgnored
        testFile.delete();

        oddjob.hardReset();

        oddjob.run();

        assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

        assertTrue(testFile.exists());
    }
}

