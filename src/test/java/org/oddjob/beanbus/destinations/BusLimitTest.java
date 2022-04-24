package org.oddjob.beanbus.destinations;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.Resettable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.state.ParentState;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class BusLimitTest extends OjTestCase {

    @Test
    public void testFiltersNoStop() {

        List<String> results = new ArrayList<>();

        BusLimit<String> test = new BusLimit<>();
        test.setLimit(2);

        test.setTo(results::add);

        BusConductor busConductor = mock(BusConductor.class);
        test.setBusConductor(busConductor);

        test.accept("Apple");
        test.accept("Orange");
        test.accept("Pear");

        assertEquals(2, results.size());

        verify(busConductor, times(1)).close();
    }

    @Test
    public void testExample() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/BusLimitExample.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> results = lookup.lookup(
                "results.beans", List.class);

        assertEquals(2, results.size());

        Object beanBus = lookup.lookup("bean-bus");
        ((Resettable) beanBus).hardReset();
        ((Runnable) beanBus).run();

        int count = lookup.lookup(
                "results.count", int.class);

        assertEquals(2, count);

        oddjob.destroy();
    }
}
