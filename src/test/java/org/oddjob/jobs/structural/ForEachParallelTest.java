package org.oddjob.jobs.structural;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.Configured;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.MockExecutorService;
import org.oddjob.scheduling.MockFuture;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.is;

public class ForEachParallelTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(
            ForEachParallelTest.class);

    @Before
    public void setUp() throws Exception {


        logger.info("----------------------------------  " + getName() +
                "  ---------------------------------------");
    }

    @Test
    public void testSimpleParallel() throws InterruptedException {

        DefaultExecutors defaultServices = new DefaultExecutors();

        String xml =
                "<foreach id='test'>" +
                        " <job>" +
                        "  <echo>${test.current}</echo>" +
                        " </job>" +
                        "</foreach>";

        ForEachJob test = new ForEachJob();
        test.setExecutorService(defaultServices.getPoolExecutor());

        ArooaSession session = new OddjobSessionFactory().createSession();

        test.setArooaSession(session);
        test.setConfiguration(new XMLConfiguration("XML", xml));

        test.setValues(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        test.setParallel(true);

        StateSteps state = new StateSteps(test);
        state.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.COMPLETE);
        test.run();

        Object[] children = OddjobTestHelper.getChildren(test);

        assertEquals(10, children.length);

        state.checkWait();

        test.destroy();

        defaultServices.stop();
    }

    @Test
    public void testStop() throws InterruptedException, FailedToStopException {

        DefaultExecutors defaultServices = new DefaultExecutors();

        String xml =
                "<foreach id='test'>" +
                        " <job>" +
                        "  <wait name='${test.current}'/>" +
                        " </job>" +
                        "</foreach>";

        ForEachJob test = new ForEachJob();
        test.setExecutorService(defaultServices.getPoolExecutor());

        ArooaSession session = new OddjobSessionFactory().createSession();

        test.setArooaSession(session);
        test.setConfiguration(new XMLConfiguration("XML", xml));

        test.setValues(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        test.setParallel(true);

        test.load();

        StateSteps state = new StateSteps(test);
        state.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        Object[] children = OddjobTestHelper.getChildren(test);

        assertEquals(10, children.length);

        StateSteps[] childChecks = new StateSteps[10];
        for (int i = 0; i < 10; ++i) {
            childChecks[i] = new StateSteps((Stateful) children[i]);
            childChecks[i].startCheck(JobState.READY, JobState.EXECUTING);
        }

        test.run();

        state.checkNow();

        // Ensure every child is executing before we stop them.
        for (int i = 0; i < 10; ++i) {
            childChecks[i].checkWait();
        }

        state.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);

        test.stop();

        // stop gets complete before check so need check wait not check now.
        state.checkWait();

        test.destroy();

        defaultServices.stop();
    }

    private static class MyExecutor extends MockExecutorService {

        List<Runnable> jobs = new ArrayList<>();

        int cancels;

        @Override
        public Future<?> submit(Runnable task) {
            jobs.add(task);
            return new MockFuture<Void>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    ++cancels;
                    return false;
                }
            };
        }
    }

    @Test
    public void testStopWithSlowStartingChild() throws FailedToStopException {

        MyExecutor executor = new MyExecutor();

        String xml =
                "<foreach id='test'>" +
                        " <job>" +
                        "  <echo>${test.current}</echo>" +
                        " </job>" +
                        "</foreach>";

        ForEachJob test = new ForEachJob();
        test.setExecutorService(executor);

        ArooaSession session = new OddjobSessionFactory().createSession();

        test.setArooaSession(session);
        test.setConfiguration(new XMLConfiguration("XML", xml));

        test.setValues(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        test.setParallel(true);

        StateSteps state = new StateSteps(test);
        state.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        test.run();

        state.checkNow();

        Object[] children = OddjobTestHelper.getChildren(test);

        assertEquals(10, children.length);

        assertEquals(10, executor.jobs.size());

        // Only execute 5 jobs.
        for (int i = 0; i < 5; ++i) {
            executor.jobs.get(i).run();
        }

        state.startCheck(ParentState.ACTIVE, ParentState.READY);

        test.stop();

        state.checkNow();

        assertEquals(10, executor.cancels);

        test.destroy();
    }

    @Test
    public void testExampleInOddjob() throws InterruptedException, FailedToStopException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/structural/ForEachParallelExample.xml",
                getClass().getClassLoader()));

        oddjob.load();

        Object foreach = OddjobTestHelper.getChildren(oddjob)[0];

        ((Loadable) foreach).load();

        Object[] children = OddjobTestHelper.getChildren(foreach);

        StateSteps wait1 = new StateSteps((Stateful) children[0]);
        StateSteps wait2 = new StateSteps((Stateful) children[1]);
        StateSteps wait3 = new StateSteps((Stateful) children[2]);

        wait1.startCheck(JobState.READY, JobState.EXECUTING);
        wait2.startCheck(JobState.READY, JobState.EXECUTING);
        wait3.startCheck(JobState.READY, JobState.EXECUTING);

        oddjob.run();

        assertEquals(ParentState.ACTIVE, oddjob.lastStateEvent().getState());

        wait1.checkWait();
        wait2.checkWait();
        wait3.checkWait();

        oddjob.stop();

        assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

        oddjob.destroy();
    }

    static final int BIG_LIST_SIZE = 20;

    public static class BigList implements Iterable<Integer> {

        private int listSize = BIG_LIST_SIZE;

        List<Integer> theList = new ArrayList<>();

        @Configured
        public void afterConfigure() {
            for (int i = 0; i < listSize; ++i) {
                theList.add(i);
            }
        }

        @Override
        public Iterator<Integer> iterator() {
            return theList.iterator();
        }

        public int getListSize() {
            return listSize;
        }

        public void setListSize(int listSize) {
            this.listSize = listSize;
        }
    }

    private static class ChildTracker implements StructuralListener {

        List<Object> children = Collections.synchronizedList(new ArrayList<>());

        Exchanger<Stateful> lastChild;

        @Override
        public void childAdded(StructuralEvent event) {

            children.add(event.getIndex(), event.getChild());

            if (lastChild != null) {
                try {
                    logger.info("* Waiting to Exchange " +
                            event.getChild().toString());

                    lastChild.exchange((Stateful) event.getChild());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void childRemoved(StructuralEvent event) {
            children.remove(event.getIndex());
        }
    }


    @Test
    public void testParallelWithWindow() throws FailedToStopException, ArooaPropertyException, ArooaConversionException, InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/structural/ForEachParallelWithWindow.xml",
                getClass().getClassLoader()));

        StateSteps oddjobState = new StateSteps(oddjob);
        oddjobState.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.COMPLETE);

        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Structural foreach = lookup.lookup("foreach",
                Structural.class);

        int preLoad = lookup.lookup("foreach.preLoad", int.class);

        ChildTracker tracker = new ChildTracker();

        foreach.addStructuralListener(tracker);

        List<?> children = tracker.children;

        assertEquals(preLoad, children.size());

        boolean executing = false;
        for (int i = 0; i < 100; ++i) {
            executing = ((Stateful) children.get(
                    children.size() - preLoad)).lastStateEvent().getState()
                    == JobState.EXECUTING;
            if (executing) {
                break;
            }
            Thread.sleep(20);
        }

        assertThat(executing, is(true));

        tracker.lastChild = new Exchanger<>();

        for (int index = 1; index < BIG_LIST_SIZE - 1; ++index) {

            if (index < 5) {
                for (int i = 0; i < index; ++i) {
                    assertEquals("Wait " + i, children.get(i).toString());
                }
            } else {
                for (int i = index - 4; i < index; ++i) {
                    assertEquals("Wait " + i, children.get(i - (index - 4)).toString());
                }
            }

            ((Stoppable) children.get(children.size() - 2)).stop();

            logger.info("* Waiting for new child after index " + index);

            Stateful lastChild = tracker.lastChild.exchange(null);

            executing = false;
            for (int i = 0; i < 100; ++i) {
                executing = lastChild.lastStateEvent().getState()
                        == JobState.EXECUTING;
                if (executing) {
                    break;
                }
                Thread.sleep(20);
            }
            assertThat(executing, is(true));
        }

        ((Stoppable) children.get(children.size() - 2)).stop();
        ((Stoppable) children.get(children.size() - 1)).stop();

        oddjobState.checkWait();

        oddjob.destroy();
    }

    @Test
    public void testParallelWithWindowStop() throws FailedToStopException, ArooaPropertyException, ArooaConversionException, InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/structural/ForEachParallelWithWindow.xml",
                getClass().getClassLoader()));

        oddjob.load();

        Stateful foreach = new OddjobLookup(oddjob).lookup("foreach",
                Stateful.class);

        ((Loadable) foreach).load();

        Object[] children = OddjobTestHelper.getChildren(foreach);

        assertEquals(2, children.length);
        assertEquals("Wait 0", children[0].toString());
        assertEquals("Wait 1", children[1].toString());

        StateSteps wait1States = new StateSteps((Stateful) children[0]);
        StateSteps wait2States = new StateSteps((Stateful) children[1]);

        wait1States.startCheck(JobState.READY, JobState.EXECUTING);
        wait2States.startCheck(JobState.READY, JobState.EXECUTING);

        oddjob.run();

        wait1States.checkWait();
        wait2States.checkWait();

        ((Stoppable) foreach).stop();

        assertEquals(ParentState.COMPLETE,
                foreach.lastStateEvent().getState());

        children = OddjobTestHelper.getChildren(foreach);

        assertEquals(2, children.length);
        assertEquals("Wait 0", children[0].toString());
        assertEquals("Wait 1", children[1].toString());

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        oddjob.destroy();
    }

}
