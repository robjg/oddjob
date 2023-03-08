package org.oddjob.beanbus.destinations;

import org.apache.commons.beanutils.DynaBean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resettable;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.adapt.ConsumerProxyGenerator;
import org.oddjob.framework.Service;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class BusQueueTest {

    private static final Logger logger = LoggerFactory.getLogger(BusQueueTest.class);

    @Rule
    public TestName name = new TestName();

    public String getName() {
        return name.getMethodName();
    }

    @Before
    public void setUp() throws Exception {


        logger.info("--------------------------  " + getName() +
                "  ------------------------");
    }

    @Test
    public void testQueueStop() throws InterruptedException {

        final BusQueue<String> test = new BusQueue<>();


        test.start();

        test.accept("apple");

        final List<String> results = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);

        Thread t = new Thread(() -> {

            for (String s : test) {
                results.add(s);
                latch.countDown();
            }
        });

        t.start();

        test.accept("pear");

        latch.await();

        // Ensure it's blocking
        Thread.sleep(100);

        test.stop();

        t.join();

        assertThat(results, contains("apple", "pear"));
    }

    @Test
    public void testStopBeforeEmpty() throws InterruptedException {

        final BusQueue<String> test = new BusQueue<>();
        test.start();

        test.accept("apple");
        test.accept("pear");

        test.stop();

        final List<String> results = new ArrayList<>();

        Thread t = new Thread(() -> {
            for (String s : test) {
                results.add(s);
            }
        });

        t.start();

        t.join();

        assertThat(results, contains("apple", "pear"));
    }

    @Test
    public void testStartConsumingFirst() throws InterruptedException {

        final BusQueue<String> test = new BusQueue<>();
        test.start();

        final List<String> results = new ArrayList<>();

        Thread t = new Thread(() -> {
            for (String s : test) {
                results.add(s);
            }
        });
        t.start();

        Thread.sleep(100);

        test.accept("apple");
        test.accept("pear");

        test.stop();

        t.join();

        assertThat(results, contains("apple", "pear"));
    }

    @Test
    public void testMultipleConsumers() throws InterruptedException {

        final BusQueue<Integer> test = new BusQueue<>();
        test.start();

        class Consumer implements Runnable {

            List<Integer> results = new ArrayList<>();

            @Override
            public void run() {
                for (Integer i : test) {
                    results.add(i);
                    Thread.yield();
                }
            }
        }

        Consumer consumer1 = new Consumer();
        Consumer consumer2 = new Consumer();
        Consumer consumer3 = new Consumer();

        Thread t1 = new Thread(consumer1);
        Thread t2 = new Thread(consumer2);
        Thread t3 = new Thread(consumer3);

        t1.start();
        t2.start();
        t3.start();

        for (int i = 1; i <= 100000; ++i) {
            test.accept(i);
        }

        Thread.sleep(50);

        test.stop();

        t1.join();
        t2.join();
        t3.join();

        logger.info("c1: " + consumer1.results.size() +
                ", c2: " + consumer2.results.size() +
                ", c3: " + consumer3.results.size());

        assertThat(consumer1.results.size() + consumer2.results.size() + consumer3.results.size(),
                is(100000));
    }

    @Test
    public void testInOddjob() throws ArooaPropertyException, ArooaConversionException, InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/BeanQueueExample.xml", getClass()
                .getClassLoader()));

        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Stateful parallel = lookup.lookup("parallel", Stateful.class);

        StateSteps parallelStates = new StateSteps(parallel);
        parallelStates.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.COMPLETE);

        oddjob.run();

        parallelStates.checkWait();

        List<?> results = lookup.lookup(
                "vars.results", List.class);

        logger.info("** Got " + results.size() + " results.");

        assertThat(results, contains("apple", "orange", "pear"));

        // We must guarantee producer runs first because it must
        // clear the queue.

        Object producer = lookup.lookup("producer");

        logger.info("** Re-run producer.");

        ((Resettable) producer).hardReset();
        ((Runnable) producer).run();

        Object consumer = lookup.lookup("consumer");

        logger.info("** Re-run consumer.");

        ((Resettable) consumer).hardReset();
        ((Runnable) consumer).run();


        results = lookup.lookup(
                "vars.results", List.class);

        logger.info("** Got " + results.size() + " results.");

        assertThat(results, contains("apple", "orange", "pear"));

        oddjob.destroy();
    }

    @Test
    public void testBeanBusExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/BeanQueueExample2.xml", getClass()
                .getClassLoader()));

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.COMPLETE);

        logger.info("** First Run.");

        oddjob.run();

        states.checkNow();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> results = lookup.lookup(
                "results.beans", List.class);

        assertThat(results, contains("Apple", "Orange", "Banana", "Pear", "Kiwi"));

        Object parallel = lookup.lookup("parallel");

        logger.info("** Reset.");

        ((Resettable) parallel).hardReset();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.READY));

        states.startCheck(ParentState.READY,
                ParentState.ACTIVE,
                ParentState.COMPLETE);

        logger.info("** Second Run.");

        ((Runnable) parallel).run();

        states.checkWait();

        logger.info("** Complete.");

        results = lookup.lookup(
                "results.beans", List.class);

        assertThat(results, contains("Apple", "Orange", "Banana", "Pear", "Kiwi"));

        oddjob.destroy();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStartConsumingBeforeStarted() throws Exception {

        ArooaSession session = new StandardArooaSession();

        final Object test = new ConsumerProxyGenerator<>(session)
                .generate(new BusQueue<>(), getClass().getClassLoader());
        ((ArooaSessionAware) test).setArooaSession(session);

        assertThat(((DynaBean) test).get("size"), is(0));

        final List<String> results1 = new ArrayList<>();

        Thread t1 = new Thread(() -> {
            for (String s : (Iterable<? extends String>) test) {
                results1.add(s);
            }
        });

        t1.start();

        Thread.sleep(100);

        ((Runnable) test).run();

        ((Consumer<String>) test).accept("apple");
        ((Consumer<String>) test).accept("pear");


        ((Service) test).stop();

        t1.join(5000L);

        assertThat(results1, contains("apple", "pear"));

        assertThat(((DynaBean) test).get("size"), is(1));
        assertThat(((DynaBean) test).get("taken"), is(2));

        ((Resettable) test).hardReset();

        assertThat(((DynaBean) test).get("size"), is(0));
        assertThat(((DynaBean) test).get("taken"), is(0));

        final List<String> results2 = new ArrayList<>();

        Thread t2 = new Thread(() -> {
            for (String s : (Iterable<? extends String>) test) {
                results2.add(s);
            }
        });

        t2.start();

        ((Runnable) test).run();

        ((Consumer<String>) test).accept("orange");
        ((Consumer<String>) test).accept("banana");

        ((Service) test).stop();

        t2.join(5000L);

        assertThat(results2, contains("orange", "banana"));
    }

}
