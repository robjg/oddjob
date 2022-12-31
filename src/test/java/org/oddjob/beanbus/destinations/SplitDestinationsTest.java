package org.oddjob.beanbus.destinations;

import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resettable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.beanbus.adapt.ConsumerProxyGenerator;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;

public class SplitDestinationsTest {

    @SuppressWarnings("unchecked")
    @Test
    public void whenChildServicesThenStartStopAsExpected() throws IOException, FailedToStopException, InterruptedException {

        ArooaSession session = mock(ArooaSession.class);

        Object service1 = new ConsumerProxyGenerator<>(session).generate(
                new BusQueue<>(),
                getClass().getClassLoader());

        Object service2 = new ConsumerProxyGenerator<>(session).generate(
                new BusQueue<>(),
                getClass().getClassLoader());

        SplitDestinations<Object> split = new SplitDestinations<>();
        split.setOf(0, service1);
        split.setOf(1, service2);

        StateSteps states = new StateSteps(split);
        states.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.STARTED);

        split.run();

        states.checkNow();

        split.accept("Apple");
        split.accept("Pear");
        split.accept("Orange");
        split.accept("Grapes");

        List<Object> results1 = new ArrayList<>();
        List<Object> results2 = new ArrayList<>();

        Thread t1 = new Thread(() -> ((Iterable<Object>) service1).forEach(results1::add));

        Thread t2 = new Thread(() -> ((Iterable<Object>) service2).forEach(results2::add));

        t1.start();
        t2.start();

        // Should block until queues are empty.
        split.flush();

        split.stop();

        t1.join(5000L);
        t2.join(5000L);

        assertThat(results1, contains("Apple", "Orange"));
        assertThat(results2, contains("Pear", "Grapes"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oddjobExample() throws InterruptedException, ArooaConversionException {

        File config = new File(Objects.requireNonNull(
                getClass().getResource("SplitAll.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(config);

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.ACTIVE,
                ParentState.COMPLETE);

        oddjob.run();

        states.checkWait();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<Integer> fibonacci = lookup.lookup(
                "fibonacci.list", List.class);

        assertThat(fibonacci, contains(1, 1, 2, 3, 5));

        List<Integer> factorial = lookup.lookup(
                "factorial.list", List.class);

        assertThat(factorial, contains(1, 2, 6, 24, 120));

        Object parallel = lookup.lookup("parallel");

        ((Resettable) parallel).hardReset();

        states.startCheck(ParentState.READY,
                ParentState.ACTIVE,
                ParentState.COMPLETE);

        ((Runnable) parallel).run();

        states.checkWait();

        fibonacci = lookup.lookup(
                "fibonacci.list", List.class);

        assertThat(fibonacci, contains(1, 1, 2, 3, 5));

        factorial = lookup.lookup(
                "factorial.list", List.class);

        assertThat(factorial, contains(1, 2, 6, 24, 120));

        oddjob.destroy();
    }
}