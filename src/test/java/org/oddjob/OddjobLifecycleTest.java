package org.oddjob;

import org.junit.Test;
import org.oddjob.arooa.ArooaConfigurationException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.NullArooaDescriptor;
import org.oddjob.arooa.deploy.annotations.ArooaElement;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.runtime.RuntimeEvent;
import org.oddjob.arooa.runtime.RuntimeListener;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.arooa.xml.XMLConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OddjobLifecycleTest extends OjTestCase {

    public static class MyValue implements ValueFactory<String> {

        private int count = 0;

        @Override
        public String toValue() throws ArooaConversionException {
            ++count;
            return "" + count;
        }
    }

    public static class MyJob implements Runnable, ArooaContextAware {

        private final List<String> events = new ArrayList<>();

        @Override
        public void setArooaContext(ArooaContext context) {
            events.add("setArooaContext");

            context.getRuntime().addRuntimeListener(new RuntimeListener() {

                @Override
                public void beforeInit(RuntimeEvent event)
                        throws ArooaConfigurationException {
                    events.add("beforeInit");
                }

                @Override
                public void beforeDestroy(RuntimeEvent event)
                        throws ArooaConfigurationException {
                    events.add("beforeDestroy");
                }

                @Override
                public void beforeConfigure(RuntimeEvent event)
                        throws ArooaConfigurationException {
                    events.add("beforeConfigure");
                }

                @Override
                public void afterInit(RuntimeEvent event)
                        throws ArooaConfigurationException {
                    events.add("afterInit");
                }

                @Override
                public void afterDestroy(RuntimeEvent event)
                        throws ArooaConfigurationException {
                    events.add("afterDestroy");
                }

                @Override
                public void afterConfigure(RuntimeEvent event)
                        throws ArooaConfigurationException {
                    events.add("afterConfigure");
                }
            });
        }

        private String value;

        @Override
        public void run() {
            events.add("run");
        }

        public String getValue() {
            return value;
        }

        @ArooaElement
        public void setValue(String value) {
            events.add("setValue " + value);
            this.value = value;
        }

        public List<String> getEvents() {
            return events;
        }

    }

    @Test
    public void testOddjobConfiguresThingsOnce() throws ArooaPropertyException, ArooaConversionException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bean class='" + MyJob.class.getName() + "' id='job'>" +
                        "   <value>" +
                        "    <bean class='" + MyValue.class.getName() + "'/>" +
                        "   </value>" +
                        "  </bean>" +
                        " </job>" +
                        "</oddjob>";

        StandardArooaSession parentSession =
                new StandardArooaSession(new NullArooaDescriptor());

        Oddjob oddjob = new Oddjob();
        oddjob.setArooaSession(parentSession);
        oddjob.setConfiguration(new XMLConfiguration("XLM", xml));

        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        assertEquals("1", lookup.lookup("job.value"));

        List<?> list = lookup.lookup("job.events", List.class);

        oddjob.destroy();

        assertEquals("setArooaContext", list.get(0));
        assertEquals("beforeInit", list.get(1));
        assertEquals("afterInit", list.get(2));
        assertEquals("beforeConfigure", list.get(3));
        assertEquals("setValue 1", list.get(4));
        assertEquals("afterConfigure", list.get(5));
        assertEquals("run", list.get(6));
        assertEquals("beforeDestroy", list.get(7));
        // Value no longer set to null on value properties
//		assertEquals("setValue null", list.get(8));
        assertEquals("afterDestroy", list.get(8));

        assertEquals(9, list.size());
    }

}
