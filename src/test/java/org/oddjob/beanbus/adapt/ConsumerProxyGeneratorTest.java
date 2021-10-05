package org.oddjob.beanbus.adapt;

import org.apache.commons.beanutils.DynaBean;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.framework.adapt.Start;
import org.oddjob.framework.adapt.Stop;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.StateSteps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

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

    <T> Object proxyFor(Consumer<T> wrapped) {

        ArooaSession session = new StandardArooaSession();

        ConsumerProxyGenerator<T> test =
                new ConsumerProxyGenerator<>(session);

        Object proxy = test.generate(wrapped, getClass().getClassLoader());

        ((ArooaSessionAware) proxy).setArooaSession(session);

        return proxy;
    }

    @Test
    public void testProxyIsRunnableStoppableAndStateful() throws FailedToStopException {

        OurDestination wrapped = new OurDestination();
        Object proxy = proxyFor(wrapped);

        assertThat(proxy, instanceOf(Runnable.class));
        assertThat(proxy, instanceOf(Stoppable.class));
        assertThat(proxy, instanceOf(Stateful.class));

        StateSteps states = new StateSteps((Stateful) proxy);
        states.startCheck(ServiceState.STARTABLE, ServiceState.STARTING, ServiceState.STARTED);

        ((Runnable) proxy).run();

        states.checkNow();

        states.startCheck(ServiceState.STARTED, ServiceState.STOPPED);

        ((Stoppable) proxy).stop();

        states.checkNow();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGeneratedProxyImplementsConsumer() throws FailedToStopException {

        OurDestination wrapped = new OurDestination();
        Object proxy = proxyFor(wrapped);

        assertThat(proxy, instanceOf(Consumer.class));

        ((Runnable) proxy).run();

        ((Consumer<String>) proxy).accept("apples");

        assertThat(wrapped.received, Matchers.contains("apples"));

        ((Stoppable) proxy).stop();
    }

    public static class BadDestination implements Consumer<String>, Runnable, AutoCloseable {

        boolean called;

        @Start
        @Override
        public void run() {
            throw new RuntimeException("Crash");
        }

        @Stop
        @Override
        public void close() throws Exception {
            throw new RuntimeException("Shouldn't be called");
        }

        @Override
        public void accept(String s) {
            called = true;
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenBusCrashedWhenGeneratedProxyConsumerAcceptThenConsumerIgnored() throws FailedToStopException {

        BadDestination wrapped = new BadDestination();
        Object proxy = proxyFor(wrapped);

        StateSteps states = new StateSteps((Stateful) proxy);
        states.startCheck(ServiceState.STARTABLE, ServiceState.STARTING, ServiceState.EXCEPTION);

        ((Runnable) proxy).run();

        states.checkNow();

        ((Consumer<String>) proxy).accept("apples");

        assertThat(wrapped.called, is(false));

        ((Stoppable) proxy).stop();
    }

    @Test
    public void testProxyIsDescribable() {

        OurDestination wrapped = new OurDestination();
        Object proxy = proxyFor(wrapped);

        assertThat(proxy, instanceOf(Describable.class));

        Map<String, String> description = ((Describable) proxy).describe();

        assertThat(description, Matchers.notNullValue());

        assertThat(description.get("fruit"), is("Apple"));
    }

    @Test
    public void testDynaBean() {

        OurDestination wrapped = new OurDestination();
        Object proxy = proxyFor(wrapped);

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
    public void testIconicBusPart() throws FailedToStopException {

        OurDestination wrapped = new OurDestination();
        Object proxy = proxyFor(wrapped);

        assertThat(proxy, instanceOf(Iconic.class));

        Iconic iconic = (Iconic) proxy;

        OurIconListener listener = new OurIconListener();

        iconic.addIconListener(listener);

        assertThat(listener.iconId, is(IconHelper.STARTABLE));

        ((Runnable) proxy).run();

        assertThat(listener.iconId, is(IconHelper.STARTED));

        ((Stoppable) proxy).stop();

        assertThat(listener.iconId, is(IconHelper.STOPPED));
    }

}
