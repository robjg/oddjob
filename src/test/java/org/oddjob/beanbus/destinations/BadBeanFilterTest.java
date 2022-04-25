package org.oddjob.beanbus.destinations;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.beanbus.BadBeanTransfer;
import org.oddjob.beanbus.SimpleBusConductor;
import org.oddjob.beanbus.drivers.IterableBusDriver;
import org.oddjob.state.ParentState;

import java.util.ArrayList;
import java.util.Arrays;
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
    public void testBecauseFilterIsStillUsedInSqlJobWithoutBeanBus() {

        List<BadBeanTransfer<String>> bad = new ArrayList<>();
        List<String> good = new ArrayList<>();

        RottenAppleDetector detector = new RottenAppleDetector();
        detector.setTo(good::add);

        BadBeanFilter<String> filter = new BadBeanFilter<>();
        filter.setBadBeanHandler(bad::add);
        filter.setTo(detector);

        IterableBusDriver<String> iterableBusDriver = new IterableBusDriver<>();
        iterableBusDriver.setValues(Arrays.asList("Good Apple", "Rotten Apple", "Good Apple"));
        iterableBusDriver.setTo(filter);

        SimpleBusConductor simpleBusConductor = new SimpleBusConductor(iterableBusDriver, filter, detector);

        simpleBusConductor.run();
        simpleBusConductor.close();

        assertThat(good, Matchers.contains("Good Apple", "Good Apple"));
        assertThat(bad, contains(new BadBeanTransfer<>("Rotten Apple", exception)));
    }

    @Test
    public void testThatOldExampleNowFlagsException() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/BadBeanFilterExample.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.EXCEPTION));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> goodBeans = lookup.lookup(
                "good.beans", List.class);

        assertThat(goodBeans, contains("Good Apple"));

        oddjob.destroy();
    }
}
