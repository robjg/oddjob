package org.oddjob.beanbus.destinations;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.Resettable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

import java.util.List;

public class UnPackTest extends OjTestCase {


    @SuppressWarnings("unchecked")
    @Test
    public void testExample() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/UnPackExample.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> results = lookup.lookup(
                "results.list", List.class);

        MatcherAssert.assertThat(results, Matchers.contains(
                "Apple", "Orange", "Pear", "Kiwi", "Banana"));


        Object beanBus = lookup.lookup("bean-bus");
        ((Resettable) beanBus).hardReset();
        ((Runnable) beanBus).run();

        int unBatcherCount = lookup.lookup(
                "unbatch.count", int.class);
        assertEquals(5, unBatcherCount);

        int resultsCount = lookup.lookup(
                "results.count", int.class);
        assertEquals(5, resultsCount);

        oddjob.destroy();
    }

}
