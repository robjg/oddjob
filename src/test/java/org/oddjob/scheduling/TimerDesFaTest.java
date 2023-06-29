/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.scheduling;

import org.junit.Test;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.*;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.job.ResetActions;
import org.oddjob.schedules.schedules.WeeklySchedule;
import org.oddjob.state.StateConditions;
import org.oddjob.tools.OddjobTestHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 *
 */
public class TimerDesFaTest extends OjTestCase {

    DesignInstance design;

    @Test
    public void testCreate() throws ArooaParseException, URISyntaxException, ParseException {

        String xml =
                "<scheduling:timer xmlns:scheduling='http://rgordon.co.uk/oddjob/scheduling'" +
                        "                  xmlns:s='http://rgordon.co.uk/oddjob/schedules'" +
                        "                  name='A Schedule'" +
                        "                  timeZone='America/Chicago'" +
                        "                  haltOn='FAILURE'" +
                        "                  reset='SOFT'" +
                        "                  haltOnFailure='true'" +
                        "                  skipMissedRuns='true'>" +
                        "   <schedule>" +
                        "    <s:weekly on=\"7\">" +
                        "     <refinement>" +
                        "     <s:time from=\"12:00\" to=\"16:00\"/>" +
                        "     </refinement>" +
                        "    </s:weekly>" +
                        "   </schedule>" +
                        "   <job>" +
                        "    <echo>Do Something Useful</echo>" +
                        "   </job>" +
                        "   <clock>" +
                        "     <bean class='org.oddjob.tools.ManualClock'>" +
                        "      <date>" +
                        "       <date date='2012-12-27 08:00'/>" +
                        "      </date>" +
                        "     </bean>" +
                        "   </clock>" +
                        "</scheduling:timer>";

        ArooaDescriptor descriptor =
                new OddjobDescriptorFactory().createDescriptor(null);

        ArooaSession session = new StandardArooaSession(descriptor);

        ArooaDescriptor sd = session.getArooaDescriptor();

        InstantiationContext instantiationContext =
                new InstantiationContext(ArooaType.COMPONENT, null);

        ArooaClass arooaClass = sd.getElementMappings(
        ).mappingFor(new ArooaElement(
                new URI("http://rgordon.co.uk/oddjob/scheduling"),
                "timer"), instantiationContext);

        assertEquals(SimpleArooaClass.class, arooaClass.getClass());

        ArooaBeanDescriptor beanDescriptor = sd.getBeanDescriptor(
                arooaClass, session.getTools().getPropertyAccessor());

        assertEquals("job", beanDescriptor.getComponentProperty());
        assertEquals(ArooaType.COMPONENT, beanDescriptor.getArooaType("job"));

        DesignParser parser = new DesignParser(session);
        parser.setArooaType(ArooaType.COMPONENT);

        parser.parse(new XMLConfiguration("TEST", xml));

        design = parser.getDesign();

        assertEquals(TimerDesign.class, design.getClass());

        Timer timer = (Timer) OddjobTestHelper.createComponentFromConfiguration(
                design.getArooaContext().getConfigurationNode());

        assertEquals("America/Chicago", timer.getTimeZone());
        assertEquals(7, ((WeeklySchedule) timer.getSchedule()).getFrom().getDayNumber());
        assertEquals(StateConditions.FAILURE, timer.getHaltOn());
        assertEquals(ResetActions.SOFT, timer.getReset());
        assertEquals(true, timer.isHaltOnFailure());
        assertEquals(true, timer.isSkipMissedRuns());
        assertEquals(1, OddjobTestHelper.getChildren(timer).length);
//		assertEquals(DateHelper.parseDateTime("2012-12-27 08:00"), 
//				timer.getClock().getDate());
    }

    public static void main(String[] args)
            throws ArooaParseException, URISyntaxException, ParseException {

        TimerDesFaTest test = new TimerDesFaTest();
        test.testCreate();

        ViewMainHelper view = new ViewMainHelper(test.design);
        view.run();
    }
}
