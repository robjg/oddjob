/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.values.properties;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaLifeAware;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.persist.MapPersister;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.oddjob.OddjobMatchers.statefulIs;

/**
 * Test for PropertiesType.
 *
 * @author rob
 */
public class PropertiesJobTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesJobTest.class);

    @Before
    public void setUp() throws Exception {
        logger.info("---------------------  " + getName() + "  ------------------");
    }

    /**
     * Test a simple set and a get.
     *
     * @throws Exception
     */
    @Test
    public void testSimpleSetGet() throws Exception {

        String xml =
                "<oddjob>\n" +
                        " <job>\n" +
                        "    <properties id='test'>\n" +
                        "     <values>\n" +
                        "      <value key='snack.favourite' value='apple'/>\n" +
                        "     </values>\n" +
                        "    </properties>\n" +
                        " </job>\n" +
                        "</oddjob>\n";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        MatcherAssert.assertThat(oddjob, statefulIs(StateConditions.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        MatcherAssert.assertThat(lookup.lookup("test.properties(snack.favourite)", String.class), is("apple"));

        // Reset

        Resettable properties = lookup.lookup("test", Resettable.class);

        properties.hardReset();

        // This isn't right? - should be null after reset?

        MatcherAssert.assertThat(lookup.lookup("test.properties(snack.favourite)", String.class), is("apple"));

        oddjob.destroy();
    }

    public static class ChangeOnConfigure implements ValueFactory<String>, ArooaLifeAware {

        private String values;

        private int count;

        private String current;

        @Override
        public String toValue() throws ArooaConversionException {
            return current;
        }

        public void setValues(String values) {
            this.values = values;
        }

        @Override
        public void initialised() {

        }

        @Override
        public void configured() {
            String[] split = values.split("\\s*,\\s*");
            current = split[count++ % split.length];
        }

        @Override
        public void destroy() {

        }
    }

    @Test
    public void testSessionPropertyLookupAddedAndRemoved() throws Exception {

        String xml =
                "<oddjob>\n" +
                        " <job>\n" +
                        "  <sequential>\n" +
                        "   <jobs>\n" +
                        "    <properties id='test'>\n" +
                        "     <values>\n" +
                        "      <bean class='" + ChangeOnConfigure.class.getName() + "' key='snack.favourite' values='apple, orange'/>\n" +
                        "     </values>\n" +
                        "    </properties>\n" +
                        "    <bean class='" + OddjobArooaTest.SessionCapture.class.getName() + "' id='sc'/>\n" +
                        "   </jobs>\n" +
                        "  </sequential>\n" +
                        " </job>\n" +
                        "</oddjob>\n";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        MatcherAssert.assertThat(oddjob, statefulIs(StateConditions.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        ArooaSession session = lookup.lookup("sc.session", ArooaSession.class);

        MatcherAssert.assertThat(session.getPropertyManager().lookup("snack.favourite"), is("apple"));

        // Note that properties aren't part of the bean directory lookup.
        MatcherAssert.assertThat(lookup.lookup("snack.favourite"), Matchers.nullValue());

        // Reset

        Object propertiesJob = lookup.lookup("test");

        ((Resettable) propertiesJob).hardReset();

        MatcherAssert.assertThat(session.getPropertyManager().lookup("snack.favourite"), nullValue());

        // Run again, expect second value.

        ((Runnable) propertiesJob).run();

        MatcherAssert.assertThat(session.getPropertyManager().lookup("snack.favourite"), is("orange"));

        oddjob.destroy();
    }

    /**
     *
     */
    @Test
    public void testPropertiesFromValues() throws Exception {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/values/properties/PropertiesJobFromValues.xml",
                getClass().getClassLoader()));

        oddjob.run();

        MatcherAssert.assertThat(oddjob, statefulIs(StateConditions.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        MatcherAssert.assertThat(lookup.lookup("echo.text", String.class), is("${snack.favourite} is apple"));

        oddjob.destroy();
    }

    /**
     *
     */
    @Test
    public void testPropertiesWithSubstitution() throws Exception {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/values/properties/PropertiesJobWithSubstitution.xml",
                getClass().getClassLoader()));

        oddjob.run();

        MatcherAssert.assertThat(oddjob, statefulIs(StateConditions.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        MatcherAssert.assertThat(lookup.lookup("echo.text", String.class), is("${snack.favourite} is apple"));

        oddjob.destroy();
    }

    /**
     * Test setting properties from an input stream.
     */
    @Test
    public void testSetFromInput() throws Exception {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/values/properties/PropertiesJobFromInput.xml",
                getClass().getClassLoader()));

        oddjob.run();

        MatcherAssert.assertThat(oddjob, statefulIs(StateConditions.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        MatcherAssert.assertThat(lookup.lookup("echo.text", String.class), is("John Smith"));

        oddjob.destroy();
    }

    public static class MyComp extends SimpleJob {
        Properties props;

        public void setProps(Properties props) {
            this.props = props;
        }

        @Override
        protected int execute() throws Throwable {
            return 0;
        }
    }

    @Test
    public void testSetPropertiesFromFile() {

        OurDirs dirs = new OurDirs();

        String xml =
                "<oddjob id='oj'>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <properties id='props'>" +
                        "     <input>" +
                        "      <file file='" + dirs.base() + "/test/types/PropertyTypeTest.props'/>" +
                        "     </input>" +
                        "    </properties>" +
                        "    <bean id='mycomp' class='" + MyComp.class.getName() + "'>" +
                        "     <props>" +
                        "      <value value='${props.properties}'/>" +
                        "     </props>" +
                        "    </bean>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob.run();

        MyComp myComp = (MyComp) new OddjobLookup(oddjob).lookup("mycomp");

        assertNotNull(myComp);
        assertEquals("test", myComp.props.get("a.b.c"));
    }

    @Test
    public void testSetFromPrevious() {
        String xml =
                "<oddjob id='oj'>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <properties>" +
                        "     <values>" +
                        "      <value key='favourite.fruit' value='apple'/>" +
                        "      <value key='snack.fruit' value='${favourite.fruit}'/>" +
                        "     </values>" +
                        "    </properties>" +
                        "    <bean id='mycomp' class='" + MyComp.class.getName() + "'>" +
                        "     <props>" +
                        "      <properties>" +
                        "       <values>" +
                        "        <value key='snack.fruit' value='${snack.fruit}'/>" +
                        "       </values>" +
                        "      </properties>" +
                        "     </props>" +
                        "    </bean>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob.run();

        MatcherAssert.assertThat(oddjob, statefulIs(StateConditions.COMPLETE));

        MyComp myComp = (MyComp) new OddjobLookup(oddjob).lookup("mycomp");

        MatcherAssert.assertThat(myComp.props.get("snack.fruit"), is("apple"));
    }


    @Test
    public void testSettingSelfFromPrevious() throws ArooaConversionException {
        String xml =
                "<oddjob id='oj'>" +
                        " <job>" +
                        "  <properties id='test'>" +
                        "   <values>" +
                        "    <value key='dist.dir' value='ojdist'/>" +
                        "    <value key='dist.name' value='oddjob-0.27.0'/>" +
                        "    <value key='dist.dir.src' value='${dist.dir}/src/${dist.name}'/>" +
                        "    <value key='dist.dir.bin' value='${dist.dir}/bin/${dist.name}'/>" +
                        "   </values>" +
                        "  </properties>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        assertEquals("ojdist/src/oddjob-0.27.0",
                lookup.lookup("test.properties(dist.dir.src)", String.class));
        assertEquals("ojdist/bin/oddjob-0.27.0",
                lookup.lookup("test.properties(dist.dir.bin)", String.class));

        Properties properties = lookup.lookup("test.properties", Properties.class);

        assertEquals(4, properties.size());
        assertEquals("ojdist", properties.getProperty("dist.dir"));
        assertEquals("oddjob-0.27.0", properties.getProperty("dist.name"));
        assertEquals("ojdist/src/oddjob-0.27.0", properties.getProperty("dist.dir.src"));
        assertEquals("ojdist/bin/oddjob-0.27.0", properties.getProperty("dist.dir.bin"));

        oddjob.destroy();
    }

    @Test
    public void testMergeFiles() throws ArooaPropertyException, ArooaConversionException {
        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <properties substitute='true'>" +
                        "     <sets>" +
                        "      <properties>" +
                        "       <input>" +
                        "        <resource resource='org/oddjob/values/properties/PropertiesTypeTest1.properties'/>" +
                        "       </input>" +
                        "      </properties>" +
                        "      <properties>" +
                        "       <input>" +
                        "        <resource resource='org/oddjob/values/properties/PropertiesTypeTest2.properties'/>" +
                        "       </input>" +
                        "      </properties>" +
                        "     </sets>" +
                        "    </properties>" +
                        "    <echo id='echo'>${snack.favourite}</echo>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("XML", xml));

        oj.run();

        String result = new OddjobLookup(oj).lookup("echo.text",
                String.class);

        assertEquals("apples", result);

        oj.destroy();
    }

    @Test
    public void testSerialization() throws ArooaPropertyException, ArooaConversionException {

        String xml1 =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <properties id='test'>" +
                        "     <values>" +
                        "      <value key='snack.favourite' value='apple'/>" +
                        "     </values>" +
                        "    </properties>" +
                        "    <variables id='vars'>" +
                        "     <result>" +
                        "      <value value='${snack.favourite}'/>" +
                        "     </result>" +
                        "    </variables>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        MapPersister persister = new MapPersister();

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml1));
        oddjob.setPersister(persister);
        oddjob.run();

        MatcherAssert.assertThat(oddjob, statefulIs(StateConditions.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Stateful test1 = lookup.lookup("test", Stateful.class);
        MatcherAssert.assertThat(test1, statefulIs(StateConditions.COMPLETE));

        MatcherAssert.assertThat(lookup.lookup("vars.result", String.class), is("apple"));

        // Second time round

        String xml2 =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <properties id='test'>" +
                        "     <values>" +
                        "      <value key='snack.favourite' value='orange'/>" +
                        "     </values>" +
                        "    </properties>" +
                        "    <variables id='vars'>" +
                        "     <result>" +
                        "      <value value='${snack.favourite}'/>" +
                        "     </result>" +
                        "    </variables>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob second = new Oddjob();

        second.setConfiguration(new XMLConfiguration("XML", xml2));
        second.setPersister(persister);

        second.load();

        MatcherAssert.assertThat(second, statefulIs(StateConditions.READY));

        lookup = new OddjobLookup(second);

        Stateful test2 = lookup.lookup("test", Stateful.class);

        MatcherAssert.assertThat(test2, statefulIs(StateConditions.COMPLETE));

        Map<String, String> description = ((Describable) test2).describe();
        MatcherAssert.assertThat(description, Matchers.hasEntry("snack.favourite", "apple"));

        MatcherAssert.assertThat(lookup.lookup("vars", Stateful.class), statefulIs(StateConditions.READY));

        MatcherAssert.assertThat(lookup.lookup("vars.result", String.class), nullValue());

        Runnable vars = lookup.lookup("vars", Runnable.class);

        vars.run();

        MatcherAssert.assertThat(lookup.lookup("vars.result", String.class), is("apple"));

        logger.info("** Now reset properties too and re-run - this time we should have orange");

        ((Resettable) test2).hardReset();
        ((Resettable) vars).hardReset();

        ((Runnable) test2).run();
        vars.run();

        MatcherAssert.assertThat(lookup.lookup("vars.result", String.class), is("orange"));

        oddjob.destroy();
    }

    @Test
    public void testOverridingProperties() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/values/properties/PropertiesJobOverriding.xml",
                getClass().getClassLoader()));

        oddjob.run();

        MatcherAssert.assertThat(oddjob, statefulIs(StateConditions.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        assertEquals("${fruit.favourite} is apple",
                lookup.lookup("echo1.text", String.class));

        assertEquals("${fruit.favourite} is apple",
                lookup.lookup("echo2.text", String.class));

        assertEquals("${fruit.favourite} is banana",
                lookup.lookup("echo3.text", String.class));

        oddjob.destroy();
    }

    @Test
    public void testDescribable() throws ArooaPropertyException, ArooaConversionException {

        String xml =
                "<oddjob>" +
                        "<job>" +
                        "<sequential>" +
                        "<jobs>" +
                        "<properties id='props1' name='Properties 1'>" +
                        "<values>" +
                        "<value key='fruit.favourite' value='apple'/>" +
                        "</values>" +
                        "</properties>" +
                        "<properties id='props2' name='Properties 2'>" +
                        "<values>" +
                        "<value key='fruit.favourite' value='pear'/>" +
                        "</values>" +
                        "</properties>" +
                        "<properties id='props3' name='Properties 3' override='true'>" +
                        "<values>" +
                        "<value key='fruit.favourite' value='banana'/>" +
                        "</values>" +
                        "</properties>" +
                        "</jobs>" +
                        "</sequential>" +
                        "</job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("TEST", xml));

        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        PropertiesJob props1 = lookup.lookup("props1", PropertiesJob.class);
        PropertiesJob props2 = lookup.lookup("props2", PropertiesJob.class);
        PropertiesJob props3 = lookup.lookup("props3", PropertiesJob.class);

        Map<String, String> description1;
        Map<String, String> description2;
        Map<String, String> description3;

        description1 = props1.describe();
        description2 = props2.describe();
        description3 = props3.describe();

        assertEquals(0, description1.size());
        assertEquals(0, description2.size());
        assertEquals(0, description3.size());

        props1.run();

        description1 = props1.describe();
        description2 = props2.describe();
        description3 = props3.describe();

        assertEquals(1, description1.size());
        assertEquals(0, description2.size());
        assertEquals(0, description3.size());
        assertEquals("apple",
                description1.get("fruit.favourite"));

        props2.run();

        description1 = props1.describe();
        description2 = props2.describe();
        description3 = props3.describe();

        assertEquals(1, description1.size());
        assertEquals(1, description2.size());
        assertEquals(0, description3.size());

        assertEquals("apple",
                description1.get("fruit.favourite"));
        assertEquals("pear *(apple) [Properties 1]",
                description2.get("fruit.favourite"));

        props1.hardReset();

        description1 = props1.describe();
        description2 = props2.describe();
        description3 = props3.describe();

        assertEquals(0, description1.size());
        assertEquals(1, description2.size());
        assertEquals(0, description3.size());

        assertEquals("pear",
                description2.get("fruit.favourite"));

        props3.run();

        description1 = props1.describe();
        description2 = props2.describe();
        description3 = props3.describe();

        assertEquals(0, description1.size());
        assertEquals(1, description2.size());
        assertEquals(1, description3.size());

        assertEquals("pear *(banana) [Properties 3]",
                description2.get("fruit.favourite"));
        assertEquals("banana",
                description3.get("fruit.favourite"));

        props1.run();
        props3.hardReset();

        description1 = props1.describe();
        description2 = props2.describe();
        description3 = props3.describe();

        assertEquals(1, description1.size());
        assertEquals(1, description2.size());
        assertEquals(0, description3.size());

        assertEquals("apple *(pear) [Properties 2]",
                description1.get("fruit.favourite"));
        assertEquals("pear",
                description2.get("fruit.favourite"));

        oddjob.destroy();
    }

    @Test
    public void testDescribeAll() throws ArooaPropertyException, ArooaConversionException {

        String xml =
                "<oddjob>" +
                        "<job>" +
                        "<sequential>" +
                        "<jobs>" +
                        "<properties id='props1' name='Properties 1'>" +
                        "<values>" +
                        "<value key='fruit.favourite' value='apple'/>" +
                        "</values>" +
                        "</properties>" +
                        "<properties id='props2' name='Properties 2'/>" +
                        "</jobs>" +
                        "</sequential>" +
                        "</job>" +
                        "</oddjob>";

        System.setProperty("props.job.test.only", "test");

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("TEST", xml));

        oddjob.run();

        MatcherAssert.assertThat(oddjob, statefulIs(StateConditions.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        PropertiesJob props1 = lookup.lookup("props1", PropertiesJob.class);
        PropertiesJob props2 = lookup.lookup("props2", PropertiesJob.class);

        Map<String, String> description1;
        Map<String, String> description2;

        description1 = props1.describe();
        description2 = props2.describe();

        assertEquals(1, description1.size());
        assertTrue(description2.size() > 1);

        assertEquals("apple [Properties 1]",
                description2.get("fruit.favourite"));
        assertEquals("test [SYSTEM]",
                description2.get("props.job.test.only"));

        oddjob.destroy();
    }

    @Test
    public void testSettingSystemProperties() throws Exception {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/values/properties/PropertiesSystem.xml",
                getClass().getClassLoader()));

        oddjob.run();

        MatcherAssert.assertThat(oddjob, statefulIs(StateConditions.COMPLETE));

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Describable test = lookup.lookup("set-system-properties", Describable.class);
        Map<String, String> testDescription = test.describe();

        assertEquals("apple", testDescription.get("oddjob.test.favourite.snack"));

        Describable allProps = lookup.lookup("all-properties", Describable.class);
        Map<String, String> description = allProps.describe();

        assertEquals("apple [SYSTEM]", description.get("oddjob.test.favourite.snack"));

        assertEquals("apple", System.getProperty("oddjob.test.favourite.snack"));

        oddjob.destroy();

        MatcherAssert.assertThat(System.getProperty("oddjob.test.favourite.snack"), nullValue());
    }
}
