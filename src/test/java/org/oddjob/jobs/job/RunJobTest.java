/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs.job;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.images.IconHelper;
import org.oddjob.jobs.WaitJob;
import org.oddjob.jobs.structural.SequentialJob;
import org.oddjob.state.*;
import org.oddjob.tools.IconSteps;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.oddjob.util.OddjobLockedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 *
 */
public class RunJobTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(RunJobTest.class);

    @Before
    public void setUp() throws Exception {


        logger.info("------------------------  " + getName() +
                "  ---------------------------");
    }

    public static class OurRunnable implements Runnable {
        int ran;

        public void run() {
            ++ran;
        }

        public int getRan() {
            return ran;
        }
    }

    @Test
    public void testCanRunAnyGivenRunnable() {

        StandardArooaSession session = new StandardArooaSession() {
            @Override
            public ComponentProxyResolver getComponentProxyResolver() {
                return new OddjobComponentResolver();
            }
        };

        OurRunnable r = new OurRunnable();

        RunJob test = new RunJob();
        test.setShowJob(true);
        test.setArooaSession(session);
        test.setJob(r);
        test.run();

        assertEquals(ParentState.COMPLETE,
                test.lastStateEvent().getState());
        assertEquals(1, r.ran);

        test.hardReset();

        assertEquals(ParentState.READY,
                test.lastStateEvent().getState());

        test.run();

        assertEquals(ParentState.COMPLETE,
                test.lastStateEvent().getState());

        assertEquals(2, r.ran);

        Object[] children = OddjobTestHelper.getChildren(test);
        assertEquals(1, children.length);
        Object proxy = children[0];

        assertEquals(JobState.COMPLETE,
                ((Stateful) proxy).lastStateEvent().getState());

        ((Resettable) proxy).hardReset();

        assertEquals(JobState.READY,
                ((Stateful) proxy).lastStateEvent().getState());

        ((Runnable) proxy).run();

        assertEquals(JobState.COMPLETE,
                ((Stateful) proxy).lastStateEvent().getState());

        assertEquals(3, r.ran);
    }

    @Test
    public void testInOddjob() throws Exception {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <bean id='r' class='" + OurRunnable.class.getName() + "'/>" +
                        "    <run id='j' job='${r}' />" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        assertEquals(1, lookup.lookup("r.ran"));

        RunJob test = lookup.lookup("j", RunJob.class);

        test.hardReset();

        test.run();

        assertEquals(1, lookup.lookup("r.ran"));

        oddjob.destroy();
    }

    @Test
    public void testDestroyAfterRunning() throws InterruptedException, ArooaPropertyException, ArooaConversionException, ArooaParseException, FailedToStopException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <folder>" +
                        "     <jobs>" +
                        "    <wait id='w'/>" +
                        "     </jobs>" +
                        "    </folder>" +
                        "    <run id='r' job='${w}' />" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Iconic wait = lookup.lookup("w", Iconic.class);

        IconSteps icons = new IconSteps(wait);

        icons.startCheck("ready", "executing", "sleeping");

        Thread t = new Thread(oddjob);
        t.start();

        icons.checkWait();

        ((Stoppable) wait).stop();

        t.join();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        logger.info("*** Cutting.");

        DragPoint dp = oddjob.provideConfigurationSession(
        ).dragPointFor(lookup.lookup("w"));

        DragTransaction trn = dp.beginChange(ChangeHow.FRESH);
        dp.delete();
        trn.commit();


        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        logger.info("Destroying Oddjob.");

        oddjob.destroy();
    }

    private static class MyStateful implements Stateful, Runnable {

        StateHandler<ServiceState> states = new StateHandler<>(
                this, ServiceState.STARTABLE);


        void fireJobState(final ServiceState state) {
            try {
                states.tryToWhen(new IsAnyState(), () -> {
                    states.setState(state);
                    states.fireEvent();
                });
            } catch (OddjobLockedException e) {
                fail(e.getMessage());
            }
        }

        @Override
        public void addStateListener(StateListener listener) {
            states.addStateListener(listener);
        }

        @Override
        public void removeStateListener(StateListener listener) {
            states.removeStateListener(listener);
        }

        @Override
        public StateEvent lastStateEvent() {
            throw new RuntimeException("Unexpected");
        }

        @Override
        public void run() {
            fireJobState(ServiceState.STARTING);
        }
    }

    @Test
    public void testDestroyedWhileActive() {

        StandardArooaSession session = new StandardArooaSession() {
            @Override
            public ComponentProxyResolver getComponentProxyResolver() {
                return new OddjobComponentResolver();
            }
        };

        MyStateful job = new MyStateful();

        RunJob test = new RunJob();
        test.setArooaSession(session);
        test.setJob(job);

        IconSteps icons = new IconSteps(test);
        icons.startCheck(IconHelper.READY, IconHelper.EXECUTING,
                IconHelper.ACTIVE);
        StateSteps states = new StateSteps(test);
        states.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE);

        test.run();

        icons.checkNow();
        states.checkNow();

        states.startCheck(ParentState.ACTIVE, ParentState.EXCEPTION);

        job.fireJobState(ServiceState.DESTROYED);

        states.checkNow();
    }

    private static class MyStateful2 extends MyStateful {

        @Override
        public void run() {
            fireJobState(ServiceState.STARTING);
            fireJobState(ServiceState.DESTROYED);
        }
    }

    @Test
    public void testDestroyedWhileExecuting() {

        MyStateful2 job = new MyStateful2();

        RunJob test = new RunJob();
        test.setJob(job);

        StateSteps states = new StateSteps(test);
        states.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.EXCEPTION);

        test.run();

        states.checkNow();


    }

    @Test
    public void testRunRemoteJob()
            throws ArooaPropertyException, ArooaConversionException,
            InterruptedException, FailedToStopException {

        File file = new File(getClass().getResource(
                "RunRemoteJob.xml").getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);
        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Iconic test = lookup.lookup("r", Iconic.class);

        Stateful wait = lookup.lookup("w", Stateful.class);

        IconSteps testIcons = new IconSteps(test);
        testIcons.startCheck("ready", "executing", "active");

        StateSteps oddjobStates = new StateSteps(oddjob);
        oddjobStates.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.ACTIVE);

        StateSteps waitStates = new StateSteps(wait);
        waitStates.startCheck(StateConditions.READY, StateConditions.EXECUTING);

        Thread t = new Thread(oddjob);
        t.start();

//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setOddjob(oddjob);
//		explorer.run();

        waitStates.checkWait();

        testIcons.checkWait();

        oddjobStates.checkWait();

        oddjobStates.startCheck(ParentState.ACTIVE, ParentState.STARTED);

        logger.info("*** Stopping Wait *** ");

        ((Stoppable) wait).stop();

        t.join(5000);

        oddjobStates.checkWait();

        oddjobStates.startCheck(ParentState.STARTED, ParentState.COMPLETE);

        logger.info("*** Stopping Oddjob *** ");

        oddjob.stop();

        oddjobStates.checkNow();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        logger.info("*** Destroying *** ");

        oddjob.destroy();
    }

    @Test
    public void testStopStopsRunningJobAndResetDoesnt() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <folder>" +
                        "     <jobs>" +
                        "	   <sequential id='s'>" +
                        "       <jobs>" +
                        "    	 <wait id='w1'/>" +
                        "    	 <wait id='w2'/>" +
                        "       </jobs>" +
                        "      </sequential>" +
                        "     </jobs>" +
                        "    </folder>" +
                        "    <run id='r' job='${s}' />" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        // wait1

        WaitJob wait1 = lookup.lookup("w1", WaitJob.class);

        IconSteps icons1 = new IconSteps(wait1);

        icons1.startCheck("ready", "executing", "sleeping");

        Thread t = new Thread(oddjob);
        t.start();

        icons1.checkWait();

        icons1.startCheck("sleeping", "stopping", "complete");

        RunJob test = lookup.lookup("r", RunJob.class);

        test.stop();

        t.join(5000);

        icons1.checkNow();

        assertEquals(ParentState.READY,
                oddjob.lastStateEvent().getState());

        // run again.
        // wait2

        WaitJob wait2 = lookup.lookup("w2", WaitJob.class);

        IconSteps icons2 = new IconSteps(wait2);

        icons2.startCheck("ready", "executing", "sleeping");

        t = new Thread(test);
        t.start();

        icons2.checkWait();

        icons2.startCheck("sleeping", "stopping", "complete");

        wait2.stop();

        t.join(5000);

        icons2.checkNow();

        assertEquals(ParentState.COMPLETE,
                test.lastStateEvent().getState());

        // reset

        test.hardReset();

        assertEquals(ParentState.READY,
                test.lastStateEvent().getState());

        SequentialJob sequential = lookup.lookup("s", SequentialJob.class);

        assertEquals(ParentState.COMPLETE,
                sequential.lastStateEvent().getState());

        logger.info("Destroying Oddjob.");

        oddjob.destroy();
    }

    @Test
    public void testResetPropertyResetsTargetJob() throws ArooaPropertyException, ArooaConversionException, InterruptedException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <folder>" +
                        "     <jobs>" +
                        "	   <echo id='e'>I've been run</echo>" +
                        "     </jobs>" +
                        "    </folder>" +
                        "    <run id='r' job='${e}' reset='HARD'/>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Stateful echo = lookup.lookup("e", Stateful.class);

        StateSteps echoStates = new StateSteps(echo);
        echoStates.startCheck(JobState.READY, JobState.EXECUTING,
                JobState.COMPLETE);

        Thread t = new Thread(oddjob);
        t.start();

        t.join(5000);

        echoStates.checkNow();

        RunJob test = lookup.lookup("r", RunJob.class);

        assertEquals(ParentState.COMPLETE,
                ((Stateful) test).lastStateEvent().getState());

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        // run again.

        ((Resettable) test).hardReset();

        echoStates.startCheck(JobState.COMPLETE, JobState.READY, JobState.EXECUTING,
                JobState.COMPLETE);

        t = new Thread(test);
        t.start();

        t.join(5000);

        echoStates.checkNow();

        assertEquals(ParentState.COMPLETE,
                test.lastStateEvent().getState());

        oddjob.destroy();
    }
}
