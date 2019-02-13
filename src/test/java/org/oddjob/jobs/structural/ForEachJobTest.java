/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.jobs.structural;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.Path;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.standard.StandardPropertyLookup;
import org.oddjob.arooa.types.XMLConfigurationType;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.images.IconHelper;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.*;
import org.oddjob.persist.MockPersisterBase;
import org.oddjob.state.*;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.IconSteps;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Executor;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author Rob Gordon.
 */
public class ForEachJobTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ForEachJobTest.class);

    @Before
    public void setUp() throws Exception {


        logger.info("--------------------  " + getName() + "  ---------------");
    }

    public static class OurJob extends SimpleJob {

        private Object stuff;
        private int index;
        private boolean ran;

        @Override
        protected int execute() throws Throwable {
            ran = true;
            return 0;
        }

        @ArooaAttribute
        public void setStuff(Object stuff) {
            this.stuff = stuff;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    private class ChildCatcher implements StructuralListener {
        final List<Object> children = new ArrayList<Object>();

        public void childAdded(StructuralEvent event) {
            children.add(event.getIndex(), event.getChild());
        }

        public void childRemoved(StructuralEvent event) {
            children.remove(event.getIndex());
        }

    }

    @Test
    public void testOneJobTwoValues() {

        String xml =
                "<foreach id='foreach'>" +
                        " <job>" +
                        "  <bean class='" + OurJob.class.getName() +
                        "' name='Our Job ${foreach.index}' stuff='${foreach.current}' index='${foreach.index}'/>" +
                        " </job>" +
                        "</foreach>";

        ForEachJob test = new ForEachJob();
        ArooaSession session = new OddjobSessionFactory().createSession();
        test.setArooaSession(session);
        test.setConfiguration(new XMLConfiguration("XML", xml));
        test.setValues(Arrays.asList("apple", "orange"));

        ChildCatcher children = new ChildCatcher();

        test.addStructuralListener(children);

        test.run();

        assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());

        assertEquals(2, children.children.size());

        OurJob job1 = (OurJob) children.children.get(0);
        OurJob job2 = (OurJob) children.children.get(1);

        assertEquals("Our Job 0", job1.toString());
        assertEquals("apple", job1.stuff);
        assertEquals(0, job1.index);
        assertTrue(job1.ran);

        assertEquals("Our Job 1", job2.toString());
        assertEquals("orange", job2.stuff);
        assertEquals(1, job2.index);
        assertTrue(job2.ran);
    }

    @Test
    public void testWithEmptyList() {

        String xml = "<foreach/>";

        ForEachJob test = new ForEachJob();
        test.setArooaSession(new OddjobSessionFactory().createSession());
        test.setConfiguration(new XMLConfiguration("XML", xml));
        test.setValues(Collections.emptyList());

        StateSteps state = new StateSteps(test);

        state.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.COMPLETE);

        test.run();

        state.checkNow();
    }

    @Test
    public void testLoadOnJobTwoValues() {

        String xml =
                "<foreach id='foreach'>" +
                        " <job>" +
                        "<bean class='" + OurJob.class.getName() +
                        "' name='Our Job ${foreach.current}' stuff='${foreach.current}' index='${foreach.index}'/>" +
                        " </job>" +
                        "</foreach>";

        ForEachJob test = new ForEachJob();
        test.setArooaSession(new OddjobSessionFactory().createSession());
        test.setConfiguration(new XMLConfiguration("XML", xml));
        test.setValues(Arrays.asList("apple", "orange"));

        ChildCatcher children = new ChildCatcher();

        test.addStructuralListener(children);

        assertTrue(test.isLoadable());

        test.load();

        assertFalse(test.isLoadable());

        assertEquals(2, children.children.size());

        OurJob job1 = (OurJob) children.children.get(0);
        OurJob job2 = (OurJob) children.children.get(1);

        assertEquals("Our Job apple", job1.toString());
        assertEquals(null, job1.stuff);
        assertEquals(0, job1.index);
        assertFalse(job1.ran);

        assertEquals("Our Job orange", job2.toString());
        assertEquals(null, job2.stuff);
        assertEquals(0, job2.index);
        assertFalse(job2.ran);

        job2.run();

        assertEquals("Our Job orange", job2.toString());
        assertEquals("orange", job2.stuff);
        assertEquals(1, job2.index);
        assertTrue(job2.ran);
    }


    public static class RegistryCheck extends SimpleJob {
        ArooaSession session;

        protected int execute() throws Throwable {
            session = getArooaSession();
            return 0;
        }
    }

    private class OurContext extends MockArooaContext {

        ArooaSession session;

        OurContext(ArooaSession session) {
            this.session = session;
        }

        @Override
        public ArooaSession getSession() {
            return session;
        }

    }

    /**
     * Tests what happens with same name. This needs a rethink when
     * the Psudo registry gets re-written.
     */
    @Test
    public void testPseudoRegistry() {

        String findMe = new String("Fruit is Healthy.");

        StandardArooaSession session = new StandardArooaSession();
        session.getBeanRegistry().register(
                "fruit", findMe);

        String xml =
                "<foreach id='test'>" +
                        " <job>" +
                        "  <bean class='" + RegistryCheck.class.getName() + "'/>" +
                        " </job>" +
                        "</foreach>";

        ForEachJob test = new ForEachJob();
        test.setValues(Arrays.asList("one"));
        test.setArooaSession(session);
        test.setConfiguration(new XMLConfiguration("XML", xml));

        // so test can configure itself when run.
        ComponentPool pool = session.getComponentPool();
        pool.registerComponent(
                new ComponentTrinity(test, test,
                        new OurContext(session) {
                            @Override
                            public RuntimeConfiguration getRuntime() {
                                return new MockRuntimeConfiguration() {
                                    @Override
                                    public void configure() {
                                    }
                                };
                            }
                        }),
                "test");

        test.run();

        ChildCatcher child = new ChildCatcher();
        test.addStructuralListener(child);

        RegistryCheck instance = (RegistryCheck) child.children.get(0);

        BeanRegistry crRecovered =
                instance.session.getBeanRegistry();

        Object bean = crRecovered.lookup("test");
        assertNotNull(bean);

        assertEquals(ForEachJob.class, bean.getClass());

        Object bean2 = crRecovered.lookup("test2");
        assertNotNull(bean2);

        assertEquals(ForEachJob.LocalBean.class, bean2.getClass());

        ForEachJob.LocalBean lb = (ForEachJob.LocalBean) bean2;

        int index = lb.getIndex();
        assertEquals(0, index);

        String current = (String) lb.getCurrent();
        assertEquals("one", current);
    }

    @Test
    public void testBasic() throws ParseException {

        checks = new Object[]{
                new String("hello"),
                DateHelper.parseDate("2005-12-25"),
                null,
                new File("file.txt")
        };
        executed = 0;

        Oddjob oj = new Oddjob();
        oj.setConfiguration(
                new XMLConfiguration("org/oddjob/jobs/structural/foreach-test.xml",
                        getClass().getClassLoader()));
        oj.run();

        // check doesn't get registered!
        Check check = (Check) new OddjobLookup(oj).lookup("check");
        assertNull(check);

        assertEquals(4, executed);

        oj.destroy();
    }

    static Object[] checks;
    static int executed;

    public static class Check extends SimpleJob {

        Object o;
        int i;

        @ArooaAttribute
        public void setObject(Object o) {
            this.o = o;
        }

        public void setIndex(int i) {
            this.i = i;
        }

        protected int execute() {
            executed++;
            logger.debug("Executing with object [" + o + "]");
            assertEquals(checks[i], o);
            return 0;
        }
    }

    @Test
    public void testReset() throws Exception {

        ChildCatcher childs = new ChildCatcher();

        String xml =
                "<foreach xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
                        "  <values>" +
                        "   <list>" +
                        "    <values>" +
                        "     <value value='COMPLETE'/>" +
                        "     <value value='INCOMPLETE'/>" +
                        "     <value value='COMPLETE'/>" +
                        "    </values>" +
                        "   </list>" +
                        "  </values>" +
                        "  <configuration>" +
                        "     <xml>" +
                        "      <foreach id='e'>" +
                        "       <job>" +
                        "        <bean class='" + FlagState.class.getName() + "' state='${e.current}'/>" +
                        "       </job>" +
                        "      </foreach>" +
                        "     </xml>" +
                        "  </configuration>" +
                        "</foreach>";

        ForEachJob test = (ForEachJob) OddjobTestHelper.createComponentFromXml(xml);
        test.addStructuralListener(childs);

        test.run();

        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(
                childs.children.get(0)));
        assertEquals(JobState.INCOMPLETE, OddjobTestHelper.getJobState(
                childs.children.get(1)));
        assertEquals(JobState.READY, OddjobTestHelper.getJobState(
                childs.children.get(2)));
        assertEquals(ParentState.INCOMPLETE, OddjobTestHelper.getJobState(
                test));

        test.softReset();

        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(
                childs.children.get(0)));
        assertEquals(JobState.READY, OddjobTestHelper.getJobState(
                childs.children.get(1)));
        assertEquals(JobState.READY, OddjobTestHelper.getJobState(
                childs.children.get(2)));
        assertEquals(ParentState.READY, OddjobTestHelper.getJobState(
                test));

        test.run();

        Stateful child1 = (Stateful) childs.children.get(0);
        Stateful child2 = (Stateful) childs.children.get(1);

        assertEquals(JobState.COMPLETE, child1.lastStateEvent().getState());
        assertEquals(JobState.INCOMPLETE, child2.lastStateEvent().getState());
        assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());

        assertEquals(3, test.getIndex());

        test.hardReset();

        assertEquals(0, test.getIndex());

        assertEquals(JobState.DESTROYED, child1.lastStateEvent().getState());
        assertEquals(JobState.DESTROYED, child2.lastStateEvent().getState());

        assertEquals(0, childs.children.size());

        test.run();

        child1 = (Stateful) childs.children.get(0);
        child2 = (Stateful) childs.children.get(1);

        assertEquals(JobState.COMPLETE, child1.lastStateEvent().getState());
        assertEquals(JobState.INCOMPLETE, child2.lastStateEvent().getState());
        assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());

        test.destroy();

        assertEquals(0, childs.children.size());

        assertEquals(JobState.DESTROYED, child1.lastStateEvent().getState());
        assertEquals(JobState.DESTROYED, child2.lastStateEvent().getState());
    }

    @Test
    public void testIdenticalIdInForEachConfig() throws Exception {

        String config =
                "<foreach id='e'>" +
                        " <job>" +
                        "  <echo>${e.current}</echo>" +
                        " </job>" +
                        "</foreach>";

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <foreach id='e'>" +
                        "   <values>" +
                        "    <list>" +
                        "     <values>" +
                        "      <value value='apple'/>" +
                        "      <value value='orange'/>" +
                        "     </values>" +
                        "    </list>" +
                        "   </values>" +
                        "   <configuration>" +
                        "    <value value='${config}'/>" +
                        "   </configuration>" +
                        "  </foreach>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        XMLConfigurationType configType = new XMLConfigurationType();
        configType.setXml(config);
        oddjob.setExport("config", configType);

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());
    }

    @Test
    public void testSerializeForEach() throws IOException, ClassNotFoundException {

        String xml =
                "<foreach>" +
                        " <job>" +
                        "  <echo/>" +
                        " </job>" +
                        "</foreach>";

        ForEachJob test = new ForEachJob();
        test.setArooaSession(new OddjobSessionFactory().createSession());
        test.setConfiguration(new XMLConfiguration("XML", xml));
        test.setValues(Arrays.asList("a", "b"));
        test.run();

        assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());

        ForEachJob copy = OddjobTestHelper.copy(test);

        assertEquals(ParentState.COMPLETE, copy.lastStateEvent().getState());

    }

    private class OurPersister extends MockPersisterBase {

        @Override
        protected void persist(Path path, String id, Object component) {
            assertEquals(new Path("foreach-job-test"), path);
            assertEquals("fe", id);
            try {
                OddjobTestHelper.copy(component);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected Object restore(Path path, String id, ClassLoader classLoader) {
            assertEquals(new Path("foreach-job-test"), path);
            assertEquals("fe", id);
            return null;
        }

    }

    @Test
    public void testForEachPersistenceButNoChildren() throws Exception {

        String config =
                "<foreach>" +
                        " <job>" +
                        "  <echo id='x'>${fe.current}</echo>" +
                        " </job>" +
                        "</foreach>";

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <foreach id='fe'>" +
                        "   <values>" +
                        "    <list>" +
                        "     <values>" +
                        "      <value value='apple'/>" +
                        "      <value value='orange'/>" +
                        "     </values>" +
                        "    </list>" +
                        "   </values>" +
                        "   <configuration>" +
                        "    <value value='${config}'/>" +
                        "   </configuration>" +
                        "  </foreach>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        XMLConfigurationType configType = new XMLConfigurationType();
        configType.setXml(config);

        oddjob.setExport("config", configType);

        OurPersister persister = new OurPersister();
        persister.setPath("foreach-job-test");
        oddjob.setPersister(persister);

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());
    }

    @Test
    public void testFileCopyExample() throws IOException {

        File toDir = OurDirs.workPathDir(getClass().getSimpleName(), true)
                .toFile();

        Properties props = new Properties();
        props.setProperty("base.dir", OurDirs.basePath().toString());
        props.setProperty("some.dir", toDir.toString());

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/structural/ForEachFilesExample.xml",
                getClass().getClassLoader()));

        oddjob.setProperties(props);

        oddjob.run();

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

        oddjob.destroy();

        assertTrue(new File(toDir, "test1.txt").exists());
        assertTrue(new File(toDir, "test2.txt").exists());
        assertTrue(new File(toDir, "test3.txt").exists());
    }

    @Test
    public void testWithIds() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/structural/ForEachWithIdsExample.xml",
                getClass().getClassLoader()));

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();
        }

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        console.dump(logger);

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Structural foreach = lookup.lookup("foreach", Structural.class);

        ChildCatcher catcher = new ChildCatcher();
        foreach.addStructuralListener(catcher);

        assertEquals(3, catcher.children.size());

        assertEquals("Red", catcher.children.get(0).toString());
        assertEquals("Blue", catcher.children.get(1).toString());
        assertEquals("Green", catcher.children.get(2).toString());

        String[] lines = console.getLines();

        assertEquals(3, lines.length);

        assertEquals("I'm number 0 and my name is Red", lines[0].trim());
        assertEquals("I'm number 1 and my name is Blue", lines[1].trim());
        assertEquals("I'm number 2 and my name is Green", lines[2].trim());

        oddjob.destroy();
    }

    @Test
    public void testPropertiesInChildren() {

        String config =
                "<foreach  id='fe'>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <properties>" +
                        "     <values>" +
                        "      <value key='my.fruit' value='${fe.current}'/>" +
                        "     </values>" +
                        "    </properties>" +
                        "    <echo>${my.fruit}</echo>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</foreach>";

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <foreach>" +
                        "   <values>" +
                        "    <list>" +
                        "     <values>" +
                        "      <value value='apple'/>" +
                        "      <value value='orange'/>" +
                        "     </values>" +
                        "    </list>" +
                        "   </values>" +
                        "   <configuration>" +
                        "    <value value='${config}'/>" +
                        "   </configuration>" +
                        "  </foreach>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        XMLConfigurationType configType = new XMLConfigurationType();
        configType.setXml(config);

        oddjob.setExport("config", configType);

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();
        }

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        console.dump(logger);

        String[] lines = console.getLines();

        assertEquals(2, lines.length);

        assertEquals("apple", lines[0].trim());
        assertEquals("orange", lines[1].trim());

        oddjob.destroy();
    }

    @Test
    public void testStop() throws InterruptedException {

        String xml =
                "<foreach id='each'>" +
                        " <job>" +
                        "  <wait name='Wait ${each.current}'/>" +
                        " </job>" +
                        "</foreach>";

        final ForEachJob test = new ForEachJob();

        test.setArooaSession(new OddjobSessionFactory().createSession());
        test.setConfiguration(new XMLConfiguration("XML", xml));

        test.setValues(Arrays.asList("apple", "orange"));

        test.addStructuralListener(new StructuralListener() {

            @Override
            public void childRemoved(StructuralEvent event) {
            }

            @Override
            public void childAdded(StructuralEvent event) {
                Stateful child = (Stateful) event.getChild();
                child.addStateListener(new StateListener() {

                    @Override
                    public void jobStateChange(StateEvent event) {
                        if (event.getState().isStoppable()) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        test.stop();
                                    } catch (FailedToStopException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }).start();
                        }
                    }
                });
            }
        });

        StateSteps testStates = new StateSteps(test);
        testStates.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.READY);
        IconSteps iconSteps = new IconSteps(test);
        iconSteps.startCheck(IconHelper.READY, IconHelper.EXECUTING,
                IconHelper.STOPPING, IconHelper.READY);

        test.run();

        testStates.checkNow();
        iconSteps.checkNow();

        Object[] children = OddjobTestHelper.getChildren(test);

        assertEquals(2, children.length);

        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(children[0]));
        assertEquals(JobState.READY, OddjobTestHelper.getJobState(children[1]));

    }

    @Test
    public void testStopWhenNotStarted() throws FailedToStopException {

        final ForEachJob test = new ForEachJob();

        StateSteps testStates = new StateSteps(test);
        testStates.startCheck(ParentState.READY);
        IconSteps iconSteps = new IconSteps(test);
        iconSteps.startCheck(IconHelper.READY);

        test.stop();

        testStates.checkNow();
        iconSteps.checkNow();
    }

    /**
     * Tracking down a bug where execution services weren't getting passed in to the
     * internal configuration.
     */
    @Test
    public void testAutoInject() {

        String forEachConfig =
                "<foreach id='test'>" +
                        " <job>" +
                        "	<state:join xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
                        "    <job>" +
                        "  <parallel>" +
                        "   <jobs>" +
                        "    <echo>Hello</echo>" +
                        "   </jobs>" +
                        "  </parallel>" +
                        "    </job>" +
                        "   </state:join>" +
                        " </job>" +
                        "</foreach>";

        String ojConfig =
                "<oddjob xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
                        " <job>" +
                        "  <foreach>" +
                        "   <values>" +
                        "    <list>" +
                        "     <values>" +
                        "      <value value='1'/>" +
                        "      <value value='2'/>" +
                        "     </values>" +
                        "    </list>" +
                        "   </values>" +
                        "   <configuration>" +
                        "    <arooa:configuration>" +
                        "     <xml>" +
                        "      <xml>" + forEachConfig + "</xml>" +
                        "     </xml>" +
                        "    </arooa:configuration>" +
                        "   </configuration>" +
                        "  </foreach>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();

        oddjob.setConfiguration(new XMLConfiguration("XML", ojConfig));

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();
        }

        String[] lines = console.getLines();
        assertEquals(2, lines.length);

        oddjob.destroy();
    }

    private static class RunNowExecutor implements Executor {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    public static class SlowToDestroyJob extends SimpleJob {
        @Override
        protected int execute() throws Throwable {
            return 0;
        }

        @Override
        protected void onDestroy() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Make sure the bug fix doesn't leave Old Jobs lying around
    private static class JobCounter implements StructuralListener {

        private Set<Object> jobs = new HashSet<Object>();

        @Override
        public void childAdded(StructuralEvent event) {
            Object child = event.getChild();
            jobs.add(child);
            if (child instanceof Structural) {
                ((Structural) child).addStructuralListener(this);
            }
        }

        @Override
        public void childRemoved(StructuralEvent event) {
            Object child = event.getChild();
            jobs.remove(child);
        }
    }

    // Ditto the JobTreeModel.
    private static class NodeCounter implements TreeModelListener {

        private Set<Object> jobs = new HashSet<Object>();

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            // Don't care about Icon notifications.
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            throw new RuntimeException("Unexpected!");
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            assertEquals(1, e.getChildren().length);
            jobs.add(e.getChildren()[0]);
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            assertEquals(1, e.getChildren().length);
            jobs.remove(e.getChildren()[0]);
        }
    }

    /**
     * Tracking down a bug where complicated structures cause an exception in explorer.
     *
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    @Test
    public void testDestroyWithComplicateStructure() throws InterruptedException, InvocationTargetException {

        String forEachConfig =
                "<foreach id='test'>" +
                        " <job>" +
                        "	<sequential>" +
                        "    <jobs>" +
                        "  <bean class='" + SlowToDestroyJob.class.getName() + "'/>" +
                        "  <bean class='" + SlowToDestroyJob.class.getName() + "'/>" +
                        "  <bean class='" + SlowToDestroyJob.class.getName() + "'/>" +
                        "  <bean class='" + SlowToDestroyJob.class.getName() + "'/>" +
                        "  <bean class='" + SlowToDestroyJob.class.getName() + "'/>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</foreach>";

        String ojConfig =
                "<oddjob xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
                        " <job>" +
                        "  <foreach id='foreach'>" +
                        "   <values>" +
                        "    <list>" +
                        "     <values>" +
                        "      <value value='1'/>" +
                        "      <value value='2'/>" +
                        "     </values>" +
                        "    </list>" +
                        "   </values>" +
                        "   <configuration>" +
                        "    <arooa:configuration>" +
                        "     <xml>" +
                        "      <xml>" + forEachConfig + "</xml>" +
                        "     </xml>" +
                        "    </arooa:configuration>" +
                        "   </configuration>" +
                        "  </foreach>" +
                        " </job>" +
                        "</oddjob>";

        final Oddjob oddjob = new Oddjob();

        oddjob.setConfiguration(new XMLConfiguration("XML", ojConfig));

        JobCounter jobCounter = new JobCounter();
        oddjob.addStructuralListener(jobCounter);

        JobTreeModel model = new JobTreeModel(new RunNowExecutor());

        NodeCounter nodeCounter = new NodeCounter();
        model.addTreeModelListener(nodeCounter);

        JobTreeNode root = new JobTreeNode(
                new MockExplorerModel() {
                    @Override
                    public Oddjob getOddjob() {
                        return oddjob;
                    }
                },
                model,
                new EventThreadLaterExecutor(),
                new ExplorerContextFactory() {
                    @Override
                    public ExplorerContext createFrom(ExplorerModel explorerModel) {
                        return new MockExplorerContext() {
                            @Override
                            public ExplorerContext addChild(Object child) {
                                return this;
                            }
                        };
                    }
                });

        root.setVisible(true);

        oddjob.run();

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                // Clear out queue.
            }
        });

        assertEquals(13, jobCounter.jobs.size());
        assertEquals(13, nodeCounter.jobs.size());

        oddjob.destroy();

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                // Clear out queue.
            }
        });

        assertEquals(0, jobCounter.jobs.size());
        assertEquals(0, nodeCounter.jobs.size());
    }

    public static class EvenNumberHater extends SimpleJob {

        private int number;

        private boolean failedOnce;

        @Override
        protected int execute() throws Throwable {
            if (number % 2 == 0 && !failedOnce) {
                failedOnce = true;
                return 1;
            }

            System.out.println(number);

            return 0;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + number;
        }
    }

    /**
     * Fixing a problem where resetting after a failure would cause
     * execution to start after the failure, not to re-run it.
     */
    @Test
    public void testRetryRetriesFromCorrectValue() {

        ChildCatcher childs = new ChildCatcher();

        String xml =
                "<foreach id='loop'>" +
                        " <job>" +
                        "  <bean class='" + EvenNumberHater.class.getName() + "' number='${loop.current}'/>" +
                        " </job>" +
                        "</foreach>";

        ForEachJob test = new ForEachJob();
        test.setArooaSession(new StandardArooaSession());
        test.setConfiguration(new XMLConfiguration("XML", xml));
        test.setValues(Arrays.asList(8, 9, 10));

        test.addStructuralListener(childs);

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            test.run();

            Stateful child1 = (Stateful) childs.children.get(0);
            Stateful child2 = (Stateful) childs.children.get(1);
            Stateful child3 = (Stateful) childs.children.get(2);

            assertEquals(JobState.INCOMPLETE,
                    child1.lastStateEvent().getState());
            assertEquals(JobState.READY,
                    child2.lastStateEvent().getState());
            assertEquals(JobState.READY,
                    child3.lastStateEvent().getState());
            assertEquals(ParentState.INCOMPLETE,
                    test.lastStateEvent().getState());

            test.softReset();

            assertEquals(JobState.READY,
                    child1.lastStateEvent().getState());
            assertEquals(JobState.READY,
                    child2.lastStateEvent().getState());
            assertEquals(JobState.READY,
                    child3.lastStateEvent().getState());
            assertEquals(ParentState.READY,
                    test.lastStateEvent().getState());

            test.run();

            assertEquals(JobState.COMPLETE,
                    child1.lastStateEvent().getState());
            assertEquals(JobState.COMPLETE,
                    child2.lastStateEvent().getState());
            assertEquals(JobState.INCOMPLETE,
                    child3.lastStateEvent().getState());
            assertEquals(ParentState.INCOMPLETE,
                    test.lastStateEvent().getState());

            test.softReset();

            assertEquals(JobState.COMPLETE,
                    child1.lastStateEvent().getState());
            assertEquals(JobState.COMPLETE,
                    child2.lastStateEvent().getState());
            assertEquals(JobState.READY,
                    child3.lastStateEvent().getState());
            assertEquals(ParentState.READY,
                    test.lastStateEvent().getState());

            test.run();

            assertEquals(JobState.COMPLETE,
                    child1.lastStateEvent().getState());
            assertEquals(JobState.COMPLETE,
                    child2.lastStateEvent().getState());
            assertEquals(JobState.COMPLETE,
                    child3.lastStateEvent().getState());
            assertEquals(ParentState.COMPLETE,
                    test.lastStateEvent().getState());

        }

        console.dump(logger);

        String[] lines = console.getLines();
        assertEquals(3, lines.length);

        assertEquals("8", lines[0].trim());
        assertEquals("9", lines[1].trim());
        assertEquals("10", lines[2].trim());

        test.destroy();
    }

    @Test
    public void testRegistryLookup() throws ArooaConversionException {

        String xml =
                "<foreach id='colour'>" +
                        " <job>" +
                        "  <echo id='echo'>Colour ${colour.current}</echo>" +
                        " </job>" +
                        "</foreach>";

        ForEachJob test = new ForEachJob();
        test.setArooaSession(new OddjobSessionFactory().createSession());
        test.setConfiguration(new XMLConfiguration("XML", xml));
        test.setValues(Arrays.asList("RED", "BLUE", "GREEN"));

        test.run();

        assertThat(test.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(test);

        assertThat(lookup.lookup("RED.index", Integer.class), is(0));
        assertThat(lookup.lookup("RED/echo.text"), is("Colour RED"));
        assertThat(lookup.lookup("BLUE.index", Integer.class), is(1));
        assertThat(lookup.lookup("BLUE/echo.text"), is("Colour BLUE"));
        assertThat(lookup.lookup("GREEN.index", Integer.class), is(2));
        assertThat(lookup.lookup("GREEN/echo.text"), is("Colour GREEN"));
    }
}
