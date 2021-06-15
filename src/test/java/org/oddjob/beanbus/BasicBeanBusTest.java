package org.oddjob.beanbus;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.beanbus.destinations.BeanCapture;

import java.util.List;
import java.util.function.Consumer;

public class BasicBeanBusTest extends OjTestCase {

    private static class OurListener implements BusListener {

        int starting;
        int beginning;
        int ending;
        int stopping;
        int crashed;
        int terminated;

        @Override
        public void busStarting(BusEvent event) throws BusCrashException {
            ++starting;
        }

        @Override
        public void tripBeginning(BusEvent event) {
            ++beginning;
        }

        @Override
        public void tripEnding(BusEvent event) throws BusCrashException {
            ++ending;
        }

        @Override
        public void busStopping(BusEvent event) {
            ++stopping;
        }

        @Override
        public void busTerminated(BusEvent event) {
            ++terminated;
        }

        @Override
        public void busCrashed(BusEvent event) {
            ++crashed;
        }

        @Override
        public void busStopRequested(BusEvent event) {
            // TODO Auto-generated method stub

        }
    }


    @Test
    public void testStandardLifecycle() throws BusException {

        BeanCapture<String> trap = new BeanCapture<>();

        OurListener listener = new OurListener();

        BasicBeanBus<String> test = new BasicBeanBus<>();
        test.setTo(trap);
        test.getBusConductor().addBusListener(listener);
        test.startBus();

        assertEquals(1, listener.starting);
        assertEquals(0, listener.beginning);

        test.accept("apple");

        assertEquals(1, listener.beginning);

        test.stopBus();

        assertEquals(1, listener.starting);
        assertEquals(1, listener.beginning);
        assertEquals(1, listener.ending);
        assertEquals(1, listener.stopping);
        assertEquals(1, listener.terminated);
        assertEquals(0, listener.crashed);

        List<String> results = trap.getBeans();

        assertEquals("apple", results.get(0));
    }

    private static class BadDestination implements Consumer<Object> {

        @Override
        public void accept(Object bean) {
            throw new IllegalArgumentException();
        }
    }

    @Test
    public void testCrashOnPutLifecycle() throws BusException {

        OurListener listener = new OurListener();

        BasicBeanBus<String> test = new BasicBeanBus<>();
        test.setTo(new BadDestination());
        test.getBusConductor().addBusListener(listener);
        test.startBus();

        assertEquals(1, listener.starting);
        assertEquals(0, listener.beginning);

        try {
            test.accept("apple");
            fail("Should fail.");
        } catch (RuntimeException e) {
            // expected
        }

        assertEquals(1, listener.starting);
        assertEquals(1, listener.beginning);
        assertEquals(0, listener.ending);
        assertEquals(0, listener.stopping);

        assertEquals(1, listener.crashed);
        assertEquals(1, listener.terminated);

        try {
            test.stopBus();
            fail("Should fail.");
        } catch (IllegalStateException e) {
            // expected
            assertEquals("Bus Not Started.", e.getMessage());
        }

    }

    @Test
    public void testCleaningLifecycle() throws BusException {

        BeanCapture<String> trap = new BeanCapture<>();

        OurListener listener = new OurListener();

        BasicBeanBus<String> test = new BasicBeanBus<>();
        test.setTo(trap);
        test.getBusConductor().addBusListener(listener);

        // start stop.

        test.startBus();

        assertEquals(1, listener.starting);

        test.stopBus();

        assertEquals(1, listener.stopping);
        assertEquals(1, listener.terminated);

        // now with clean.
        test.startBus();

        assertEquals(2, listener.starting);

        test.accept("apple");

        assertEquals(1, listener.beginning);

        test.getBusConductor().cleanBus();

        assertEquals(1, listener.ending);

        test.accept("pear");

        assertEquals(2, listener.beginning);

        test.getBusConductor().cleanBus();

        assertEquals(2, listener.ending);

        test.stopBus();

        assertEquals(2, listener.starting);
        assertEquals(2, listener.beginning);
        assertEquals(2, listener.ending);
        assertEquals(2, listener.stopping);
        assertEquals(2, listener.terminated);

        // no clean before stop
        test.startBus();

        test.accept("orange");

        test.getBusConductor().cleanBus();

        test.accept("kiwi");

        test.stopBus();

        assertEquals(3, listener.starting);
        assertEquals(4, listener.beginning);
        assertEquals(4, listener.ending);
        assertEquals(3, listener.stopping);
        assertEquals(3, listener.terminated);
    }

    @Test
    public void testBusState() throws BusException {

        BeanCapture<String> trap = new BeanCapture<>();

        OurListener listener = new OurListener();

        BasicBeanBus<String> test = new BasicBeanBus<>();
        test.setTo(trap);
        test.getBusConductor().addBusListener(listener);

        // stop before started..

        try {
            test.stopBus();
            fail("Should fail!");
        } catch (IllegalStateException e) {
            // expected.
        }

        assertEquals(0, listener.starting);
        assertEquals(0, listener.beginning);
        assertEquals(0, listener.ending);
        assertEquals(0, listener.stopping);
        assertEquals(0, listener.terminated);

        test.startBus();

        assertEquals(1, listener.starting);

        // start after started.
        try {
            test.startBus();
            fail("Should fail!");
        } catch (IllegalStateException e) {
            // expected.
        }

        test.stopBus();

        assertEquals(1, listener.starting);
        assertEquals(0, listener.beginning);
        assertEquals(0, listener.ending);
        assertEquals(1, listener.stopping);
        assertEquals(1, listener.terminated);
    }

    private static class CrashingOnStartListener extends OurListener {

        @Override
        public void busStarting(BusEvent event) throws BusCrashException {

            throw new BusCrashException("Bang!");
        }
    }

    @Test
    public void testCrashedByListenerWhenStarting() throws BusCrashException {

        CrashingOnStartListener listener = new CrashingOnStartListener();

        BasicBeanBus<String> test = new BasicBeanBus<>();

        test.getBusConductor().addBusListener(listener);

        try {
            test.startBus();
            fail("Should Throw Exception");
        } catch (BusCrashException e) {
            assertEquals("Bang!", e.getMessage());
        }

        assertEquals(0, listener.starting);
        assertEquals(0, listener.beginning);
        assertEquals(0, listener.ending);
        assertEquals(0, listener.stopping);
        assertEquals(1, listener.terminated);
        assertEquals(1, listener.crashed);

        try {
            test.accept("apple");
            fail("Should Throw Exception");
        } catch (IllegalStateException e) {
            // expected.
            assertEquals("Bus Not Started.", e.getMessage());
        }

        try {
            test.stopBus();
            fail("Should Throw Exception");
        } catch (IllegalStateException e) {
            // expected.
            assertEquals("Bus Not Started.", e.getMessage());
        }
    }

    private static class CrashingOnCleanListener extends OurListener {

        @Override
        public void tripEnding(BusEvent event) throws BusCrashException {
            throw new BusCrashException("Bang!");
        }

    }

    @Test
    public void testCrashedByListenerWhenCleaning() throws BusCrashException {

        CrashingOnCleanListener listener = new CrashingOnCleanListener();

        BasicBeanBus<String> test = new BasicBeanBus<>();

        test.getBusConductor().addBusListener(listener);

        test.startBus();

        assertEquals(1, listener.starting);
        assertEquals(0, listener.beginning);

        test.accept("apple");

        assertEquals(1, listener.beginning);

        try {
            test.getBusConductor().cleanBus();
        } catch (BusCrashException e) {
            assertEquals("Bang!", e.getMessage());
        }

        try {
            test.stopBus();
            fail("Should Throw Exception");
        } catch (IllegalStateException e) {
            // expected.
            assertEquals("Bus Not Started.", e.getMessage());
        }

        assertEquals(0, listener.stopping);
        assertEquals(0, listener.ending);
        assertEquals(1, listener.terminated);
        assertEquals(1, listener.crashed);
    }
}
