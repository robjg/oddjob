package org.oddjob.beanbus.bus;

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

public class BeanMapTest {


    @SuppressWarnings("unchecked")
    @Test
    public void testExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/bus/BeanMapExample.xml", getClass()
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

        assertThat(results.get(0).getPrice(), is(51.0));
        assertThat(results.get(1).getPrice(), is(72.4));
        assertThat(results.get(2).getPrice(), is(80.8));

        Object beanBus = lookup.lookup("bean-bus");

        ((Resettable) beanBus).hardReset();
        ((Runnable) beanBus).run();

        results = lookup.lookup(
                "results.beans", List.class);

        // demonstrates a gotcha in Arooa. The bean price isn't reconfigured in the list because the
        // values are constant, so the doubled values don't get reset.
        // To avoid this situation the Beans should have been immutable and the function should have created
        // new ones.
        assertThat(results.get(0).getPrice(), is(102.0));
        assertThat(results.get(1).getPrice(), is(144.8));
        assertThat(results.get(2).getPrice(), is(161.6));

        oddjob.destroy();
    }
}
