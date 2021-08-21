package org.oddjob.beanbus.destinations;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resettable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.example.Fruit;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BeanFilterTest {


    @SuppressWarnings("unchecked")
    @Test
    public void testExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/BeanFilterExample.xml", getClass()
                .getClassLoader()));

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.COMPLETE);

        oddjob.run();

        states.checkNow();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<Fruit> results = lookup.lookup(
                "results.beans", List.class);

        assertThat(results.size(), is(2));

        Object beanBus = lookup.lookup("bean-bus");

        ((Resettable) beanBus).hardReset();
        ((Runnable) beanBus).run();

        results = lookup.lookup(
                "results.beans", List.class);

        assertThat(results.size(), is(2));

        oddjob.destroy();
    }
}
