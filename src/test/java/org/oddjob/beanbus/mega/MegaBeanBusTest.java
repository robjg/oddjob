package org.oddjob.beanbus.mega;

import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.Destination;
import org.oddjob.beanbus.bus.BasicBusService;
import org.oddjob.beanbus.bus.BusSessionFactory;
import org.oddjob.beanbus.drivers.IterableBusDriver;
import org.oddjob.framework.Service;
import org.oddjob.images.IconHelper;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.appender.AppenderArchiver;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.IconSteps;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.beans.ExceptionListener;
import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MegaBeanBusTest {

    private static final Logger logger = LoggerFactory.getLogger(MegaBeanBusTest.class);

    @Test
    public void testSimpleLifecycle() {

        RuntimeConfiguration runtimeConfiguration = mock(RuntimeConfiguration.class);

        ArooaSession session = new BusSessionFactory()
                .createSession(new OddjobSessionFactory().createSession(),
                        getClass().getClassLoader());

        ArooaContext arooaContext = mock(ArooaContext.class);
        when(arooaContext.getRuntime()).thenReturn(runtimeConfiguration);
        when(arooaContext.getSession()).thenReturn(session);

        List<String> destination = new ArrayList<>();

        IterableBusDriver<String> driver = new IterableBusDriver<>();
        driver.setValues(Arrays.asList("apple", "pear", "banana"));

        Object driverProxy = session.getComponentProxyResolver().resolve(driver, session);
        ((ArooaSessionAware) driverProxy).setArooaSession(session);

        session.getComponentPool().registerComponent(
                ComponentTrinity.withComponent(driver)
                        .andProxy(driverProxy)
                        .andArooaContext(arooaContext),
                null);

        Consumer<String> consumer = destination::add;

        driver.setTo(consumer);

        Object destinationProxy = session.getComponentProxyResolver().resolve(consumer, session);

        BasicBusService test = new BasicBusService();
        test.setArooaSession(session);
        test.setExecutor(Runnable::run);
        test.setOf(0, driverProxy);
        test.setOf(1, destinationProxy);

        StateSteps destinationSteps = new StateSteps((Stateful) destinationProxy);
        destinationSteps.startCheck(ServiceState.STARTABLE, ServiceState.STARTING,
                ServiceState.STARTED, ServiceState.STOPPED);

        test.run();

        destinationSteps.checkNow();

        assertThat(test.lastStateEvent().getState(), is(ParentState.COMPLETE));
        assertThat(destination, contains("apple", "pear", "banana"));
    }


    public static class NumberGenerator implements Runnable {

        private Consumer<? super Integer> to;

        @Override
        public void run() {

            for (int i = 0; i < 100; ++i) {
                to.accept(i);
            }
        }

        public void setTo(Consumer<? super Integer> to) {
            this.to = to;
        }
    }

    public static class OurDestination implements Consumer<Integer>, Service {

        int total;

        boolean started;

        boolean stopped;

        @Override
        public void start() throws Exception {
            started = true;
            total = 0;
        }

        @Override
        public void stop() throws FailedToStopException {
            stopped = true;
        }

        @Override
        public void accept(Integer e) {
            total = total + e;
        }

        public int getTotal() {
            return total;
        }

        public boolean isStarted() {
            return started;
        }

        public boolean isStopped() {
            return stopped;
        }
    }

    @Test
    public void testWithNoBusConductor() throws ArooaPropertyException, ArooaConversionException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bus:bus id='test' xmlns:bus=\"oddjob:beanbus\">" +
                        "   <of>" +
                        "    <bean class='" + NumberGenerator.class.getName() + "'>" +
                        "     <to><value value='${results}'/></to>" +
                        "    </bean>" +
                        "    <bean class='" + OurDestination.class.getName() + "' " +
                        "          id='results'/>" +
                        "   </of>" +
                        "  </bus:bus>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Iconic results = (Iconic) lookup.lookup("results");

        IconSteps icons = new IconSteps(results);
        icons.startCheck(IconHelper.STARTABLE,
                IconHelper.EXECUTING, IconHelper.STARTED,
                IconHelper.STOPPING, IconHelper.STOPPED);

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        icons.checkNow();

        assertThat(lookup.lookup("results.total", int.class), is(4950));
        assertThat(lookup.lookup("results.started", boolean.class), is(true));
        assertThat(lookup.lookup("results.stopped", boolean.class), is(true));

        Object test = lookup.lookup("test");
        ((Resettable) test).hardReset();
        ((Runnable) test).run();

        assertThat(lookup.lookup("results.total", int.class), is(4950));

        oddjob.destroy();
    }

    public static class OurSlipperyDestination implements Consumer<Integer>, ExceptionListener, Service {

        int crashed;
        int terminated;

        @Override
        public void exceptionThrown(Exception e) {
            ++crashed;
        }

        @Override
        public void stop() throws FailedToStopException {
            ++terminated;
        }

        @Override
        public void start() throws Exception {
            throw new BusCrashException("Slippery Destination!");
        }

        @Override
        public void accept(Integer e) {
            throw new RuntimeException("Unexpected.");
        }

        public int getCrashed() {
            return crashed;
        }

        public int getTerminated() {
            return terminated;
        }
    }

    @Test
    public void testWithBadBusPart() throws ArooaPropertyException, ArooaConversionException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bus:bus id='test' xmlns:bus=\"oddjob:beanbus\">" +
                        "   <of>" +
                        "    <bean class='" + NumberGenerator.class.getName() + "'>" +
                        "     <to><value value='${results}'/></to>" +
                        "    </bean>" +
                        "    <bean class='" + OurSlipperyDestination.class.getName() + "' " +
                        "          id='results'/>" +
                        "   </of>" +
                        "  </bus:bus>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Iconic results = (Iconic) lookup.lookup("results");

        IconSteps icons = new IconSteps(results);
        icons.startCheck(IconHelper.STARTABLE,
                IconHelper.EXECUTING, IconHelper.EXCEPTION);

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.EXCEPTION));

        icons.checkNow();

        // close and crash not called on the thing that crashed.
        assertThat(lookup.lookup("results.crashed", int.class), is(1));
        assertThat(lookup.lookup("results.terminated", int.class), is(0));

        Object test = lookup.lookup("test");
        ((Resettable) test).hardReset();
        ((Runnable) test).run();

        assertThat(lookup.lookup("results.crashed", int.class), is(2));
        assertThat(lookup.lookup("results.terminated", int.class), is(0));


        oddjob.destroy();
    }

    public static class OutboundCapture implements Consumer<String> {

        private final List<Collection<String>> outbounds =
                new ArrayList<>();

        @Override
        public void accept(String e) {
            outbounds.get(outbounds.size() - 1).add(e);
        }

        @Destination
        public void setOutbound(Collection<String> outbound) {
            this.outbounds.add(outbound);
        }

        public List<Collection<String>> getOutbounds() {
            return outbounds;
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNoAutoLink() throws ArooaPropertyException, ArooaConversionException {

        List<String> ourList = new ArrayList<>();

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/mega/MegaBeanBusNoAutoLink.xml",
                getClass().getClassLoader()));
        oddjob.setExport("our-list", new ArooaObject(ourList));
        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<List<String>> outbounds = lookup.lookup("capture.outbounds", List.class);

        assertThat(outbounds.size(), is(1));

        List<String> results = outbounds.get(0);

        assertThat(ourList, sameInstance(results));

        assertThat(results.get(0), is("Apple"));
        assertThat(results.get(1), is("Orange"));
        assertThat(results.get(2), is("Pear"));
        assertThat(results.size(), is(3));

        List<String> outboundList = lookup.lookup("list.beans", List.class);
        assertThat(outboundList.size(), is(0));

        Object bus = lookup.lookup("bus");

        ((Resettable) bus).hardReset();

        ((Runnable) bus).run();

        outbounds = lookup.lookup("capture.outbounds", List.class);

        assertThat(outbounds.size(), is(3));

        results = outbounds.get(2);

        assertThat(ourList, sameInstance(results));

        oddjob.destroy();
    }

    public static class ThingWithOutbound {

        private Consumer<String> outbound;

        public Consumer<String> getOutbound() {
            return outbound;
        }

        public void setOutbound(Consumer<String> outbound) {
            this.outbound = outbound;
        }
    }

    public static class ComplicatedOutbound implements Consumer<String> {

        private ThingWithOutbound thing;

        @Override
        public void accept(String e) {
            thing.outbound.accept(e);
        }

        public ThingWithOutbound getThing() {
            return thing;
        }


        public void setThing(ThingWithOutbound thing) {
            this.thing = thing;
        }

        @Destination
        public void acceptOutbound(Consumer<String> outbound) {
            this.thing.setOutbound(outbound);
        }

    }

    /**
     * Need to ensure the order of configuration and auto linking is OK.
     */
    @Test
    public void testWithComplicatedOutbound() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/mega/MegaBeanBusComplexOutbound.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        @SuppressWarnings("unchecked")
        List<String> results = lookup.lookup("list.beans", List.class);

        assertThat(results.get(0), is("Apple"));
        assertThat(results.get(1), is("Orange"));
        assertThat(results.get(2), is("Pear"));
        assertThat(results.size(), is(3));

        @SuppressWarnings("unchecked")
        Consumer<String> outbound = lookup.lookup(
                "capture.thing.outbound", Consumer.class);

        @SuppressWarnings("unchecked")
        Consumer<String> beanCapture = lookup.lookup(
                "list", Consumer.class);

        assertThat(beanCapture, sameInstance(outbound));

        oddjob.destroy();
    }

    public static class DestinationWithLogger
            implements Consumer<String>, Service, Flushable, ExceptionListener {

        private AutoCloseable closeable;

        @Override
        public void start() throws Exception {
            logger.info("** The Bus is Starting.");
        }

        @Override
        public void flush() throws IOException {
            logger.info("** A Trip is Ending.");
        }

        @Override
        public void stop() throws FailedToStopException {
            logger.info("** The Bus is Stopping.");

        }

        @Override
        public void exceptionThrown(Exception e) {
            logger.info("** The Bus has Crashed.");
        }

        @Override
        public void accept(String e) {
            logger.info("** We have received " + e + ".");

            if ("crash-the-bus".equals(e)) {
                throw new IllegalArgumentException(e);
            } else {
                try {
                    closeable.close();
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        }

        @Inject
        public void setBusConductor(AutoCloseable stopBus) {
            this.closeable = stopBus;
        }

        @Override
        public String toString() {
            return "OurLoggingThing";
        }
    }

//    public static class MaybeCrash implements Service, Consumer<String> {
//
//
//    }

    /**
     * Test logging. Note that this test is fragile with respect to change
     * in the logging properties.
     */
    @Test
    public void testDefaultLogger() {

        final List<String> messages = new ArrayList<>();

        class MyLogListener implements LogListener {
            public void logEvent(LogEvent logEvent) {
                String message = logEvent.getMessage().trim();
                if (message.startsWith("** ")) {
                    messages.add(message);
                }
            }
        }

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/mega/MegaLoggerTest.xml",
                getClass().getClassLoader()));

        oddjob.load();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.READY));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Object thingWithLogging = lookup.lookup("thing-with-logging");

        String loggerName = ((LogEnabled) thingWithLogging).loggerName();

        assertThat(loggerName.substring(0, DestinationWithLogger.class.getName().length()),
                is(DestinationWithLogger.class.getName()));

        LoggerAdapter.appenderAdapterFor(loggerName).setLevel(LogLevel.INFO);

        AppenderArchiver archiver = new AppenderArchiver(thingWithLogging, "%m%n");

        MyLogListener ll = new MyLogListener();
        archiver.addLogListener(ll, thingWithLogging, LogLevel.DEBUG, 0, 1000);

        oddjob.run();

        assertThat(ParentState.COMPLETE, is(oddjob.lastStateEvent().getState()));

        assertThat(messages, contains("** The Bus is Starting.",
                "** We have received Apples.",
                "** A Trip is Ending.",
                "** The Bus is Stopping."));

        Object secondBus = lookup.lookup("second-bus");

        ((Resettable) secondBus).hardReset();
        messages.clear();

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.EXCEPTION));

        assertThat(messages, contains("** The Bus is Starting.",
                "** We have received crash-the-bus.",
                "** The Bus has Crashed."));

        oddjob.destroy();
    }

    @Test
    public void testCutPasteBusPartIsFine() throws ArooaParseException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/mega/MegaBusCutTest.xml",
                getClass().getClassLoader()));
        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        Object busPart = new OddjobLookup(
                oddjob).lookup("bus-part");

        DragPoint busPartPoint = oddjob.provideConfigurationSession().dragPointFor(
                busPart);

        DragTransaction trn = busPartPoint.beginChange(ChangeHow.FRESH);
        String copy = busPartPoint.copy();
        busPartPoint.delete();
        trn.commit();

        Object bus = new OddjobLookup(
                oddjob).lookup("bus");

        DragPoint busPoint = oddjob.provideConfigurationSession().dragPointFor(
                bus);

        trn = busPoint.beginChange(ChangeHow.FRESH);
        busPoint.paste(-1, copy);
        trn.commit();

        Object driver = new OddjobLookup(
                oddjob).lookup("driver");

        ((Resettable) driver).hardReset();
        ((Runnable) bus).run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        oddjob.destroy();
    }

}
