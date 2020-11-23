package org.oddjob.beanbus.destinations;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.Resettable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.state.ParentState;

import java.util.List;

public class BadBeanFilterTest extends OjTestCase {

    public static class RottonAppleDetector
            extends AbstractFilter<String, String> {

        @Override
        protected String filter(String from) {
            if (from.startsWith("Rotton")) {
                throw new IllegalArgumentException(
                        "Rotton Apples Spoil The Barrel.");
            } else {
                return from;
            }
        }
    }

    @Test
    public void testExample() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/BadBeanFilterExample.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> goodBeans = lookup.lookup(
                "good.beans", List.class);

        List<?> badBeans = lookup.lookup(
                "bad.beans", List.class);

        assertEquals(2, goodBeans.size());

        assertEquals("Good Apple", goodBeans.get(0));
        assertEquals("Good Apple", goodBeans.get(1));

        assertEquals(1, badBeans.size());

        Object driver = lookup.lookup("driver");
        ((Resettable) driver).hardReset();
        ((Runnable) driver).run();

        int beanCount = lookup.lookup("filter.count", int.class);
        int badBeanCount = lookup.lookup("filter.badCount", int.class);

        assertEquals(3, beanCount);
        assertEquals(1, badBeanCount);

        oddjob.destroy();
    }
}
