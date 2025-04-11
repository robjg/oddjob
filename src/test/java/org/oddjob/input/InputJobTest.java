package org.oddjob.input;

import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.runtime.PropertyLookup;
import org.oddjob.arooa.standard.MockPropertyLookup;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.persist.MapPersister;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

import java.io.File;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class InputJobTest extends OjTestCase {

    private static class OurInputHandler implements InputHandler {

        @Override
        public Session start() {
            return new Session() {
                @Override
                public Properties handleInput(InputRequest[] requests) {

                    Properties properties = new Properties();

                    properties.setProperty("favourite.fruit", "apples");

                    return properties;
                }

                @Override
                public void close() {

                }
            };
        }

    }

    @Test
    public void testFullLifeCycle() throws ArooaPropertyException, ArooaConversionException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <input id='input'/>" +
                        " </job>" +
                        "</oddjob>";

        OddjobSessionFactory sessionFactory = new OddjobSessionFactory();
        ArooaSession session = sessionFactory.createSession();

        session.getPropertyManager().addPropertyLookup(new MockPropertyLookup() {

            @Override
            public String lookup(String propertyName) {
                assertEquals("favourite.fruit", propertyName);
                return "pears";
            }
        });

        Oddjob oddjob = new Oddjob();
        oddjob.setArooaSession(session);
        oddjob.setInheritance(OddjobInheritance.SHARED);
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob.setInputHandler(new OurInputHandler());

        PropertyLookup lookup = session.getPropertyManager();

        assertEquals("pears", lookup.lookup("favourite.fruit"));

        oddjob.run();

        assertEquals("apples", lookup.lookup("favourite.fruit"));

        Resettable resettable = session.getBeanRegistry().lookup(
                "input", Resettable.class);

        resettable.hardReset();

        assertEquals("pears", lookup.lookup("favourite.fruit"));

        oddjob.run();

        assertEquals("apples", lookup.lookup("favourite.fruit"));

        oddjob.destroy();

        assertEquals("pears", lookup.lookup("favourite.fruit"));
    }

    @Test
    public void testSerialisable() throws ArooaPropertyException, ArooaConversionException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <input id='input'>" +
                        "   <requests>" +
                        "    <input-message>Hi!</input-message>" +
                        "   </requests>" +
                        "  </input>" +
                        " </job>" +
                        "</oddjob>";

        MapPersister persister = new MapPersister();

        OddjobSessionFactory sessionFactory = new OddjobSessionFactory();
        ArooaSession session1 = sessionFactory.createSession();

        Oddjob oddjob1 = new Oddjob();
        oddjob1.setArooaSession(session1);
        oddjob1.setInheritance(OddjobInheritance.SHARED);
        oddjob1.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob1.setInputHandler(new OurInputHandler());
        oddjob1.setPersister(persister);

        oddjob1.run();

        assertEquals(ParentState.COMPLETE, oddjob1.lastStateEvent().getState());

        oddjob1.destroy();

        ArooaSession session2 = sessionFactory.createSession();

        Oddjob oddjob2 = new Oddjob();
        oddjob2.setArooaSession(session2);
        oddjob2.setInheritance(OddjobInheritance.SHARED);
        oddjob2.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob2.setInputHandler(new OurInputHandler());
        oddjob2.setPersister(persister);

        oddjob2.load();

        InputJob test = new OddjobLookup(oddjob2).lookup("input", InputJob.class);

        assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());

        PropertyLookup lookup = session2.getPropertyManager();

        assertEquals("apples", lookup.lookup("favourite.fruit"));

        test.hardReset();

        oddjob2.run();

        assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());

        oddjob2.destroy();
    }

    static class WaitInputHandler implements InputHandler {

        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        @Override
        public Session start() {
            return new Session() {

                @Override
                public Properties handleInput(InputRequest[] requests) {
                    latch1.countDown();
                    try {
                        latch2.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }

                @Override
                public void close() {
                    latch2.countDown();
                }
            };
        }
    }

    @Test
    public void testStop() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {

        File file = new File(Objects.requireNonNull(
                getClass().getResource("InputHandlerWithStop.xml")).getFile());

        Properties props = new Properties();
        props.setProperty("prop.run.timer", "false");

        WaitInputHandler inputHandler = new WaitInputHandler();

        Oddjob oddjob = new Oddjob();
        oddjob.setInheritance(OddjobInheritance.SHARED);
        oddjob.setFile(file);
        oddjob.setProperties(props);
        oddjob.setInputHandler(inputHandler);


        StateSteps steps = new StateSteps(oddjob);
        steps.startCheck(StateSteps.definitely(ParentState.READY),
                StateSteps.definitely(ParentState.EXECUTING));

        Thread t = new Thread(oddjob);
        t.start();

        inputHandler.latch1.await();

        steps.checkNow();

        steps.startCheck(
                StateSteps.definitely(ParentState.EXECUTING),
                StateSteps.definitely(ParentState.INCOMPLETE));

        new OddjobLookup(oddjob).lookup("input", Stoppable.class).stop();

        steps.checkWait();

        oddjob.destroy();

    }

}
