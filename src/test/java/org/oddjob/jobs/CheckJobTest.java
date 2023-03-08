package org.oddjob.jobs;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.MockArooaTools;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckJobTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(CheckJobTest.class);

    @Before
    public void setUp() throws Exception {
        logger.debug("------------- " + getName() + " ----------------");
    }

    @Test
    public void testNoValue() {

        CheckJob test = new CheckJob();

        test.run();

        assertEquals(1, test.getResult());

        test.setNull(true);

        test.run();

        assertEquals(0, test.getResult());
    }

    static class OurSession extends MockArooaSession {

        @Override
        public ArooaTools getTools() {
            return new MockArooaTools() {
                @Override
                public ArooaConverter getArooaConverter() {
                    return new DefaultConverter();
                }
            };
        }
    }

    @Test
    public void testIntegerEq() {

        CheckJob test = new CheckJob();
        test.setArooaSession(new OurSession());

        test.setValue(2);
        test.setEq(new ArooaObject(2));

        test.run();

        assertEquals(0, test.getResult());

        test.setEq(new ArooaObject(4));
        test.run();

        assertEquals(1, test.getResult());

        test.setEq(new ArooaObject(4.0F));
        test.run();

        assertEquals(1, test.getResult());
    }

    @Test
    public void testIntegerNe() {

        CheckJob test = new CheckJob();
        test.setArooaSession(new OurSession());

        test.setValue(2);
        test.setNe(new ArooaObject("2"));

        test.run();

        assertEquals(1, test.getResult());

        test.setNe(new ArooaObject(4));
        test.run();

        assertEquals(0, test.getResult());

        test.setNe(new ArooaObject(4.0F));
        test.run();

        assertEquals(0, test.getResult());
    }

    @Test
    public void testIntegerLt() {

        CheckJob test = new CheckJob();
        test.setArooaSession(new OurSession());

        test.setValue(2);
        test.setLt(new ArooaObject(4));

        test.run();

        assertEquals(0, test.getResult());

        test.setLt(new ArooaObject(2));
        test.run();

        assertEquals(1, test.getResult());

        test.setLt(new ArooaObject(4.0F));

        test.run();

        assertEquals(0, test.getResult());
    }

    @Test
    public void testIntegerLe() {

        CheckJob test = new CheckJob();
        test.setArooaSession(new OurSession());

        test.setValue(2);
        test.setLe(new ArooaObject(4));

        test.run();

        assertEquals(0, test.getResult());

        test.setLe(new ArooaObject(2));
        test.run();

        assertEquals(0, test.getResult());

        test.setLe(new ArooaObject(4.0F));

        test.run();

        assertEquals(0, test.getResult());
    }

    @Test
    public void testIntegerGt() {

        CheckJob test = new CheckJob();
        test.setArooaSession(new OurSession());

        test.setValue(6);
        test.setGt(new ArooaObject(4));

        test.run();

        assertEquals(0, test.getResult());

        test.setGt(new ArooaObject(6));
        test.run();

        assertEquals(1, test.getResult());

        test.setGt(new ArooaObject(4.0F));

        test.run();

        assertEquals(0, test.getResult());
    }

    @Test
    public void testIntegerGe() {

        CheckJob test = new CheckJob();
        test.setArooaSession(new OurSession());

        test.setValue(6);
        test.setGe(new ArooaObject(4));

        test.run();

        assertEquals(0, test.getResult());

        test.setGe(new ArooaObject(6));
        test.run();

        assertEquals(0, test.getResult());

        test.setGe(new ArooaObject(4.0F));

        test.run();

        assertEquals(0, test.getResult());
    }

    @Test
    public void testStringEq() {

        CheckJob test = new CheckJob();
        test.setArooaSession(new OurSession());

        test.setValue("apples");
        test.setEq(new ArooaObject("apples"));

        test.run();

        assertEquals(0, test.getResult());

        test.setEq(new ArooaObject("oranges"));
        test.run();

        assertEquals(1, test.getResult());
    }

    @Test
    public void testStringNe() {

        CheckJob test = new CheckJob();
        test.setArooaSession(new OurSession());

        test.setValue("apples");
        test.setNe(new ArooaObject("apples"));

        test.run();

        assertEquals(1, test.getResult());

        test.setNe(new ArooaObject("oranges"));
        test.run();

        assertEquals(0, test.getResult());
    }

    @Test
    public void testStringLt() {

        CheckJob test = new CheckJob();
        test.setArooaSession(new OurSession());

        test.setValue("apples");
        test.setLt(new ArooaObject("oranges"));

        test.run();

        assertEquals(0, test.getResult());

        test.setLt(new ArooaObject("apples"));
        test.run();

        assertEquals(1, test.getResult());

        test.setLt(new ArooaObject("APPLES"));

        test.run();

        assertEquals(1, test.getResult());
    }

    @Test
    public void testStringGt() {

        CheckJob test = new CheckJob();
        test.setArooaSession(new OurSession());

        test.setValue("apples");
        test.setGt(new ArooaObject("APPLES"));

        test.run();

        assertEquals(0, test.getResult());

        test.setGt(new ArooaObject("apples"));
        test.run();

        assertEquals(1, test.getResult());

        test.setGt(new ArooaObject("oranges"));

        test.run();

        assertEquals(1, test.getResult());

        // proving the documentation.

        test.setValue("999");
        test.setGt(new ArooaObject(1000));
        test.run();

        assertEquals(0, test.getResult());
    }

    public void checkZeroLengthStrings() {

        CheckJob test = new CheckJob();
        test.setArooaSession(new OurSession());

        test.setValue("apples");
        test.setZ(false);
        test.setNull(false);

        test.run();

        assertEquals(0, test.getResult());

        test.setValue("");

        test.run();

        assertEquals(1, test.getResult());

        test.setValue(null);

        test.run();

        assertEquals(1, test.getResult());

        test.setValue("apples");

        test.setZ(true);

        test.run();

        assertEquals(1, test.getResult());
    }

    @Test
    public void testTextExample() {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/CheckTextExample.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        oddjob.destroy();
    }

    @Test
    public void testTextIncompleteExample() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/CheckTextIncompleteExample.xml",
                getClass().getClassLoader()));

        StateSteps state = new StateSteps(oddjob);
        state.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.INCOMPLETE);

        oddjob.run();

        state.checkNow();

        Structural structural = new OddjobLookup(oddjob).lookup(
                "all-checks", Structural.class);

        Object[] children = OddjobTestHelper.getChildren(structural);

        for (Object child : children) {
            assertEquals(JobState.INCOMPLETE,
                    ((Stateful) child).lastStateEvent().getState());
        }

        oddjob.destroy();
    }

    @Test
    public void testNumberExample() {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/CheckNumberExample.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        oddjob.destroy();
    }

    @Test
    public void testExistsExample() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/CheckExistsExample.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertEquals(ParentState.INCOMPLETE,
                oddjob.lastStateEvent().getState());

        assertEquals(JobState.COMPLETE,
                new OddjobLookup(oddjob).lookup("should-complete",
                        Stateful.class).lastStateEvent().getState());

        assertEquals(JobState.INCOMPLETE,
                new OddjobLookup(oddjob).lookup("should-incomplete",
                        Stateful.class).lastStateEvent().getState());

        oddjob.destroy();
    }
}
