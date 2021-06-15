package org.oddjob.beanbus.mega;

import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.parsing.*;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.*;
import org.oddjob.beanbus.drivers.IterableBusDriver;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
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

        ArooaContext arooaContext = mock(ArooaContext.class);
        when(arooaContext.getRuntime()).thenReturn(runtimeConfiguration);

        ArooaSession session = new MegaBusSessionFactory()
                .createSession(new OddjobSessionFactory().createSession(),
                        getClass().getClassLoader());
        List<String> destination = new ArrayList<>();

        IterableBusDriver<String> driver = new IterableBusDriver<>();
        driver.setBeans(Arrays.asList("apple", "pear", "banana"));

        Object driverProxy = session.getComponentProxyResolver().resolve(driver, session);
        session.getComponentPool().registerComponent(
                ComponentTrinity.withComponent(driver)
                        .andProxy(driverProxy)
                        .andArooaContext(arooaContext),
                null);

        Consumer<String> consumer = destination::add;

        driver.setTo(consumer);

        Object destinationProxy = session.getComponentProxyResolver().resolve(consumer, session);

        MegaBeanBus test = new MegaBeanBus();
        test.setArooaSession(session);
        test.setParts(0, driverProxy);
        test.setParts(1, destinationProxy);

        StateSteps destinationSteps = new StateSteps((Stateful) destinationProxy);
        destinationSteps.startCheck(ServiceState.STARTABLE, ServiceState.STARTING,
                ServiceState.STARTED, ServiceState.STOPPED);

        test.run();

        destinationSteps.checkNow();

        assertThat(test.lastStateEvent().getState(), is(ParentState.COMPLETE));
        assertThat(destination, contains("apple", "pear", "banana"));
    }

    @Test
    public void testExample() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/mega/MegaBeanBusExample.xml",
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

        oddjob.destroy();
    }

    @Test
    public void testConfigurationSession() throws URISyntaxException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/mega/MegaBeanBusExample.xml",
                getClass().getClassLoader()));
        oddjob.setExport("beans", new ArooaObject(
                Collections.EMPTY_LIST));

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        ConfigurationOwner test = (ConfigurationOwner) lookup.lookup("bus");

        ConfigurationSession session = test.provideConfigurationSession();

        ArooaDescriptor descriptor = session.getArooaDescriptor();

        ArooaClass cl = descriptor.getElementMappings().mappingFor(
                new ArooaElement(new URI("oddjob:beanbus"), "bean-copy"),
                new InstantiationContext(ArooaType.COMPONENT, null));

        assertThat(cl, notNullValue());

        oddjob.destroy();
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

    public static class OurDestination extends AbstractDestination<Integer> {

        int total;

        boolean started;

        boolean stopped;


        final TrackingBusListener busListener = new TrackingBusListener() {
            @Override
            public void busStarting(BusEvent event) {
                started = true;
                total = 0;
            }

            @Override
            public void busStopping(BusEvent event) {
                stopped = true;
            }
        };

        @Override
        public void accept(Integer e) {
            total = total + e;
        }

        @Inject
        public void setBusConductor(BusConductor busConductor) {
            busListener.setBusConductor(busConductor);
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
                        "  <bean-bus id='test'>" +
                        "   <parts>" +
                        "    <bean class='" + NumberGenerator.class.getName() + "'>" +
                        "     <to><value value='${results}'/></to>" +
                        "    </bean>" +
                        "    <bean class='" + OurDestination.class.getName() + "' " +
                        "          id='results'/>" +
                        "   </parts>" +
                        "  </bean-bus>" +
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

    public static class OurSliperyDestination extends AbstractDestination<Integer> {

        int crashed;
        int terminated;


        final TrackingBusListener busListener = new TrackingBusListener() {
            @Override
            public void busStarting(BusEvent event) throws BusCrashException {
                throw new BusCrashException("Slippery Destination!");
            }

            @Override
            public void busCrashed(BusEvent event) {
                ++crashed;
            }

            @Override
            public void busTerminated(BusEvent event) {
                ++terminated;
            }
        };

        @Override
        public void accept(Integer e) {
            throw new RuntimeException("Unexpected.");
        }

        @Inject
        public void setBusConductor(BusConductor busConductor) {
            busListener.setBusConductor(busConductor);
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
                        "  <bean-bus id='test'>" +
                        "   <parts>" +
                        "    <bean class='" + NumberGenerator.class.getName() + "'>" +
                        "     <to><value value='${results}'/></to>" +
                        "    </bean>" +
                        "    <bean class='" + OurSliperyDestination.class.getName() + "' " +
                        "          id='results'/>" +
                        "   </parts>" +
                        "  </bean-bus>" +
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

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.EXCEPTION));

        icons.checkNow();

        assertThat(lookup.lookup("results.crashed", int.class), is(1));
        assertThat(lookup.lookup("results.terminated", int.class), is(1));

        Object test = lookup.lookup("test");
        ((Resettable) test).hardReset();
        ((Runnable) test).run();

        assertThat(lookup.lookup("results.crashed", int.class), is(2));
        assertThat(lookup.lookup("results.terminated", int.class), is(2));


        oddjob.destroy();
    }

    public static class OutboundCapture extends AbstractDestination<String> {

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

    public static class ComplicatedOutbound extends AbstractDestination<String> {

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

    public static class DestinationWithLogger extends AbstractDestination<String> {

        final TrackingBusListener busListener = new TrackingBusListener() {
            @Override
            public void busStarting(BusEvent event) {
                logger.info("** The Bus is Starting.");
            }

            public void tripBeginning(BusEvent event) {
                logger.info("** A Trip is Beginning.");
            }

            public void tripEnding(BusEvent event) {
                logger.info("** A Trip is Ending.");

            }

            public void busStopRequested(BusEvent event) {
                logger.info("** A Bus Stop is Requested.");
            }

            @Override
            public void busStopping(BusEvent event) {
                logger.info("** The Bus is Stopping.");
            }

            public void busCrashed(BusEvent event) {
                logger.info("** The Bus has Crashed.");
            }

            public void busTerminated(BusEvent event) {
                logger.info("** The Bus has terminated.");
            }

            @Override
            public String toString() {
                return "OurLoggingBusListener";
            }
        };

        @Override
        public void accept(String e) {
            logger.info("** We have received " + e + ".");

            if ("crash-the-bus".equals(e)) {
                throw new IllegalArgumentException(e);
            } else {
                busListener.getBusConductor().requestBusStop();
            }
        }

        @Inject
        public void setBusConductor(BusConductor busConductor) {
            busListener.setBusConductor(busConductor);
        }

        @Override
        public String toString() {
            return "OurLoggingThing";
        }
    }

    /**
     * Test logging. Note that this test is fragile with respect to change
     * in the logging properties.
     */
    @Test
    public void testDefaultLogger() {

        final List<String> messages = new ArrayList<>();

        class MyLogListener implements LogListener {
            public void logEvent(LogEvent logEvent) {
                messages.add(logEvent.getMessage().trim());
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

        assertThat(messages, hasItem("** The Bus is Starting."));
        assertThat(messages, hasItem("** A Trip is Beginning."));
        assertThat(messages, hasItem("** We have received Apples."));
        assertThat(messages, hasItem("** A Bus Stop is Requested."));
        assertThat(messages, hasItem("** A Trip is Ending."));
        assertThat(messages, hasItem("** The Bus is Stopping."));
        assertThat(messages, hasItem("** The Bus has terminated."));

        Object secondBus = lookup.lookup("second-bus");

        ((Resettable) secondBus).hardReset();
        messages.clear();

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.EXCEPTION));

        assertThat(messages, hasItem("** The Bus is Starting."));
        assertThat(messages, hasItem("** A Trip is Beginning."));
        assertThat(messages, hasItem("** We have received crash-the-bus."));
        assertThat(messages, hasItem("** The Bus has Crashed."));
        assertThat(messages, hasItem("** The Bus has terminated."));

        oddjob.destroy();
    }

    @Test
    public void testCutPasteBusPartInvalidatesBus() throws ArooaParseException {

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
        ((Runnable) driver).run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.EXCEPTION));

        oddjob.destroy();
    }

}
