package org.oddjob.framework.adapt.job;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.adapt.Run;
import org.oddjob.state.StateConditions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.oddjob.OddjobMatchers.statefulIs;

public class RunMethodTest {

    public static class OurTask {

        boolean ran;

        int status;

        @Run
        public Integer go() {
            ran = true;
            return status;
        }

        public boolean isRan() {
            return ran;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String toString() {
            return "OurTask";
        }
    }

    @Test
    public void testOurTaskInOddjob() throws ArooaConversionException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bean class='" + OurTask.class.getName() + "' id='r' />" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Object runnable = lookup.lookup("r");
        assertThat((Stateful) runnable, statefulIs(StateConditions.COMPLETE));

        Boolean ran = lookup.lookup("r.ran", Boolean.class);

        assertThat(ran, is(true));

        oddjob.destroy();
    }

    @Test
    public void testIncompleteInOddjob() throws Exception {
        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bean class='" + OurTask.class.getName() +
                        "' id='r' status='10'/>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Object runnable = lookup.lookup("r");
        assertThat((Stateful) runnable, statefulIs(StateConditions.INCOMPLETE));

        Boolean ran = lookup.lookup("r.ran", Boolean.class);

        assertThat(ran, is(true));

        oddjob.destroy();
    }

    public static class OurTask2  {

        boolean ran;

        @Run
        public void go() {
            ran = true;
        }

        public boolean isRan() {
            return ran;
        }

        public String toString() {
            return "OurTask2";
        }
    }

    @Test
    public void testVoidCallableInOddjob() throws Exception {
        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bean class='" + OurTask2.class.getName() +
                        "' id='r' />" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Object runnable = lookup.lookup("r");
        assertThat((Stateful) runnable, statefulIs(StateConditions.COMPLETE));

        Boolean ran = lookup.lookup("r.ran", Boolean.class);

        assertThat(ran, is(true));

        oddjob.destroy();
    }

    public static class BadTask  {

        @Run
        public void go() throws Exception {
            throw new Exception("Deliberate fail!");
        }

        public String toString() {
            return "BadTask";
        }
    }

    @Test
    public void testExceptionInOddjob() {
        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bean class='" + BadTask.class.getName() +
                        "' id='r'/>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Object runnable = lookup.lookup("r");
        assertThat((Stateful) runnable, statefulIs(StateConditions.EXCEPTION));

        oddjob.destroy();
    }

}
