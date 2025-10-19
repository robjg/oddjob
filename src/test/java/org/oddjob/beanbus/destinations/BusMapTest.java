package org.oddjob.beanbus.destinations;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resettable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.beanbus.example.Fruit;
import org.oddjob.script.ScriptJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class BusMapTest {


    @SuppressWarnings("unchecked")
    @Test
    public void testExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException {

        File config = new File(Objects.requireNonNull(
                getClass().getResource("BeanTransformerExample.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(config);

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.COMPLETE);

        oddjob.run();

        states.checkNow();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<Fruit> results = lookup.lookup(
                "results.list", List.class);

        assertThat(results, Matchers.contains(51.0, 72.4, 80.8));

        Object beanBus = lookup.lookup("bean-bus");

        ((Resettable) beanBus).hardReset();
        ((Runnable) beanBus).run();

        results = lookup.lookup(
                "results.list", List.class);

        assertThat(results, Matchers.contains(51.0, 72.4, 80.8));

        oddjob.destroy();
    }

    @Test
    public void givenScriptFunctionThenUsedOk() {

        ArooaSession arooaSession = new StandardArooaSession();

        ScriptJob scriptJob = new ScriptJob();
        scriptJob.setArooaSession(arooaSession);
        scriptJob.setScript(
                "function addTwo(x) { return new java.lang.Integer(x + 2)}\n" +
                        "function multiplyByTwo(x) { return new java.lang.Integer(x * 2)}");
        scriptJob.run();

        assertThat(scriptJob.lastStateEvent().getState(), is(JobState.COMPLETE));

        BusMap<Object, Object> busMap = new BusMap<>();
        busMap.setFunction(scriptJob.getFunction("addTwo"));

        List<Object> results = new ArrayList<>();

        busMap.setTo(results::add);

        busMap.accept(2);
        busMap.accept(5);

        assertThat(results, contains(4, 7));
    }
}
