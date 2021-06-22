package org.oddjob.beanbus.destinations;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resettable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BadBeanTransfer;
import org.oddjob.state.ParentState;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class BadBeanFilterTest {

    private final static IllegalArgumentException exception = new IllegalArgumentException(
            "Rotten Apples Spoil The Barrel.");

    public static class RottenAppleDetector
            extends AbstractFilter<String, String> {

        @Override
        protected String filter(String from) {
            if (from.startsWith("Rotten")) {
                throw exception;
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

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> goodBeans = lookup.lookup(
                "good.beans", List.class);

        List<?> badBeans = lookup.lookup(
                "bad.beans", List.class);

        assertThat(goodBeans, contains("Good Apple", "Good Apple"));
        assertThat(badBeans, contains(new BadBeanTransfer<>("Rotten Apple", exception)));

        Object bus = lookup.lookup("bus");
        ((Resettable) bus).hardReset();
        ((Runnable) bus).run();

        int beanCount = lookup.lookup("filter.count", int.class);
        int badBeanCount = lookup.lookup("filter.badCount", int.class);

        assertThat(beanCount, is(3));
        assertThat(badBeanCount, is(1));

        oddjob.destroy();
    }
}
