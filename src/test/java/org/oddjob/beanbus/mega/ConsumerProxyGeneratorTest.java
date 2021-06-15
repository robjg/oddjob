package org.oddjob.beanbus.mega;

import org.apache.commons.beanutils.DynaBean;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.beanbus.BasicBeanBus;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusListenerAdapter;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class ConsumerProxyGeneratorTest {

    public static class OurDestination implements Consumer<String> {

        private int number;

        private final List<String> received = new ArrayList<>();

        @Override
        public void accept(String s) {
            received.add(s);
        }

        public String getFruit() {
            return "Apple";
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }

    OurDestination wrapped;

    Object proxy;

    @Before
    public void setUp() throws Exception {


        ArooaSession session = new StandardArooaSession();

        ConsumerProxyGenerator<String> test =
                new ConsumerProxyGenerator<>(session);

        wrapped = new OurDestination();

        proxy = test.generate(wrapped, getClass().getClassLoader());

        ((ArooaSessionAware) proxy).setArooaSession(session);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGeneratedProxyImplementsRunnable() throws BusCrashException {

        assertThat(proxy, instanceOf(Consumer.class));

        BasicBeanBus<String> bus = new BasicBeanBus<>();

        ((BusPart) proxy).prepare(bus.getBusConductor());

        bus.startBus();

        ((Consumer<String>) proxy).accept("apples");

        assertThat(wrapped.received, Matchers.contains("apples"));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testGeneratedProxyImplementsConsumer() throws BusCrashException {

        assertThat(proxy, instanceOf(Consumer.class));

        BasicBeanBus<String> bus = new BasicBeanBus<>();

        ((BusPart) proxy).prepare(bus.getBusConductor());

        bus.startBus();

        ((Consumer<String>) proxy).accept("apples");

        assertThat(wrapped.received, Matchers.contains("apples"));
    }

    @Test
    public void givenBusCrashedWhenGeneratedProxyConsumerAcceptThenException() {

        BasicBeanBus<String> bus = new BasicBeanBus<>();
        bus.getBusConductor().addBusListener(new BusListenerAdapter() {
            @Override
            public void busStarting(BusEvent event) throws BusCrashException {
                throw new BusCrashException("Crash");
            }
        });

        ((BusPart) proxy).prepare(bus.getBusConductor());

        try {
            bus.startBus();
            fail("Should Throw Exception");
        } catch (BusCrashException e) {
            assertThat(e.getMessage(), is("Crash"));
        }

        try {
            //noinspection unchecked
            ((Consumer<String>) proxy).accept("apples");
            fail("Should Throw Exception");
        } catch (RuntimeException e) {
            assertThat(e.getCause().getCause().getCause().getMessage(), is("Crash"));
        }
    }

    @Test
    public void testProxyIsRunnableStoppableAndStateful() {

        assertThat(proxy, instanceOf(Runnable.class));
        assertThat(proxy, instanceOf(Stoppable.class));
        assertThat(proxy, instanceOf(Stateful.class));

        ((Runnable) proxy).run();
    }

    @Test
    public void testProxyIsDescribable() {

        assertThat(proxy, instanceOf(Describable.class));

        Map<String, String> description = ((Describable) proxy).describe();

        assertThat(description, Matchers.notNullValue());

        assertThat(description.get("fruit"), is("Apple"));
    }

    @Test
    public void testDynaBean() {

        assertThat(proxy, instanceOf(DynaBean.class));

        DynaBean dynaBean = ((DynaBean) proxy);

        assertThat(dynaBean.get("fruit"), is("Apple"));

        dynaBean.set("number", 3);

        assertThat(dynaBean.get("number"), is(3));
    }

    static class OurIconListener implements IconListener {

        String iconId;

        @Override
        public void iconEvent(IconEvent e) {
            iconId = e.getIconId();
        }
    }

    @Test
    public void testIconicBusPart() throws BusCrashException, FailedToStopException {

        assertThat(proxy, instanceOf(Iconic.class));

        Iconic iconic = (Iconic) proxy;

        OurIconListener listener = new OurIconListener();

        iconic.addIconListener(listener);

        assertThat(listener.iconId, is(IconHelper.STARTABLE));

        BusPart busPart = (BusPart) proxy;

        BasicBeanBus<String> bus = new BasicBeanBus<>();

        busPart.prepare(bus.getBusConductor());

        assertThat(listener.iconId, is(IconHelper.STARTABLE));

        bus.startBus();

        ((Runnable) proxy).run();

        assertThat(listener.iconId, is(IconHelper.STARTED));

        bus.stopBus();

        ((Stoppable) proxy).stop();

        assertThat(listener.iconId, is(IconHelper.STOPPED));
    }

}
