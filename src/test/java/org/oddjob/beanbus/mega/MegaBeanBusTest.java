package org.oddjob.beanbus.mega;

import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.parsing.*;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.*;
import org.oddjob.beanbus.drivers.IterableBusDriver;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.appender.AppenderArchiver;
import org.oddjob.state.ParentState;
import org.oddjob.tools.IconSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;

public class MegaBeanBusTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(MegaBeanBusTest.class);

    @Test
    public void testSimpleLifecycle() {


        ArooaSession session = new OddjobSessionFactory().createSession();

        List<String> destination = new ArrayList<>();

        IterableBusDriver<String> driver = new IterableBusDriver<>();
        driver.setBeans(Arrays.asList("apple", "pear", "banana"));
        driver.setTo(destination::add);

        MegaBeanBus test = new MegaBeanBus();
        test.setArooaSession(session);
        test.setParts(0, driver);
        test.setParts(1, destination);

        test.run();

        assertEquals(ParentState.COMPLETE,
                test.lastStateEvent().getState());


    }

    @Test
    public void testExample() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/mega/MegaBeanBusExample.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        @SuppressWarnings("unchecked")
        List<String> results = lookup.lookup("list.beans", List.class);

        assertEquals("Apple", results.get(0));
        assertEquals("Orange", results.get(1));
        assertEquals("Pear", results.get(2));
        assertEquals(3, results.size());

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

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        ConfigurationOwner test = (ConfigurationOwner) lookup.lookup("bus");

        ConfigurationSession session = test.provideConfigurationSession();

        ArooaDescriptor descriptor = session.getArooaDescriptor();

        ArooaClass cl = descriptor.getElementMappings().mappingFor(
                new ArooaElement(new URI("oddjob:beanbus"), "bean-copy"),
                new InstantiationContext(ArooaType.COMPONENT, null));

        assertNotNull(cl);

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
        icons.startCheck(CollectionWrapper.INACTIVE,
                CollectionWrapper.ACTIVE, CollectionWrapper.INACTIVE);

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        icons.checkNow();

        assertEquals(Integer.valueOf(4950), lookup.lookup(
                "results.total", Integer.class));
        assertEquals(Boolean.TRUE, lookup.lookup(
                "results.started", boolean.class));
        assertEquals(Boolean.TRUE, lookup.lookup(
                "results.stopped", boolean.class));

        Object test = lookup.lookup("test");
        ((Resettable) test).hardReset();
        ((Runnable) test).run();

        assertEquals(Integer.valueOf(4950), lookup.lookup(
                "results.total", Integer.class));

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
        icons.startCheck(CollectionWrapper.INACTIVE,
                CollectionWrapper.ACTIVE, CollectionWrapper.INACTIVE);

        oddjob.run();

        assertEquals(ParentState.EXCEPTION,
                oddjob.lastStateEvent().getState());

        icons.checkNow();

        assertEquals(Integer.valueOf(1), lookup.lookup(
                "results.crashed", Integer.class));
        assertEquals(Integer.valueOf(1), lookup.lookup(
                "results.terminated", Integer.class));

        Object test = lookup.lookup("test");
        ((Resettable) test).hardReset();
        ((Runnable) test).run();

        assertEquals(Integer.valueOf(2), lookup.lookup(
                "results.crashed", Integer.class));
        assertEquals(Integer.valueOf(2), lookup.lookup(
                "results.terminated", Integer.class));


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

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<List<String>> outbounds = lookup.lookup("capture.outbounds", List.class);

        assertEquals(1, outbounds.size());

        List<String> results = outbounds.get(0);

        assertSame(ourList, results);

        assertEquals("Apple", results.get(0));
        assertEquals("Orange", results.get(1));
        assertEquals("Pear", results.get(2));
        assertEquals(3, results.size());

        List<String> outboundList = lookup.lookup("list.beans", List.class);
        assertEquals(0, outboundList.size());

        Object bus = lookup.lookup("bus");

        ((Resettable) bus).hardReset();

        ((Runnable) bus).run();

        outbounds = lookup.lookup("capture.outbounds", List.class);

        assertEquals(3, outbounds.size());

        results = outbounds.get(2);

        assertSame(ourList, results);

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

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        @SuppressWarnings("unchecked")
        List<String> results = lookup.lookup("list.beans", List.class);

        assertEquals("Apple", results.get(0));
        assertEquals("Orange", results.get(1));
        assertEquals("Pear", results.get(2));
        assertEquals(3, results.size());

        @SuppressWarnings("unchecked")
        Consumer<String> outbound = lookup.lookup(
                "capture.thing.outbound", Consumer.class);

        @SuppressWarnings("unchecked")
        Consumer<String> beanCapture = lookup.lookup(
                "list", Consumer.class);

        assertSame(beanCapture, outbound);

        oddjob.destroy();
    }

    public static class DestinationWithLogger extends AbstractDestination<String> {

        final TrackingBusListener busListener = new TrackingBusListener() {
            @Override
            public void busStarting(BusEvent event) {
                logger.info("The Bus is Starting.");
            }

            public void tripBeginning(BusEvent event) {
                logger.info("A Trip is Beginning.");
            }

            public void tripEnding(BusEvent event) {
                logger.info("A Trip is Ending.");

            }

            public void busStopRequested(BusEvent event) {
                logger.info("A Bus Stop is Requested.");
            }

            @Override
            public void busStopping(BusEvent event) {
                logger.info("The Bus is Stopping.");
            }

            public void busCrashed(BusEvent event) {
                logger.info("The Bus has Crashed.");
            }

            public void busTerminated(BusEvent event) {
                logger.info("The Bus has terminated.");
            }
        };

        @Override
        public void accept(String e) {
            logger.info("We have received " + e + ".");

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

    }

    /**
     * Test logging. Note that this test is fragile with respect to change
     * in the logging properties.
     *
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

        assertEquals(ParentState.READY,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Object thingWithLogging = lookup.lookup("thing-with-logging");

        String loggerName = ((LogEnabled) thingWithLogging).loggerName();

        assertEquals(DestinationWithLogger.class.getName(),
                loggerName.substring(0, DestinationWithLogger.class.getName().length()));

        LoggerAdapter.appenderAdapterFor(loggerName).setLevel(LogLevel.INFO);

        AppenderArchiver archiver = new AppenderArchiver(thingWithLogging, "%m%n");

        MyLogListener ll = new MyLogListener();
        archiver.addLogListener(ll, thingWithLogging, LogLevel.DEBUG, 0, 1000);

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        assertEquals("The Bus is Starting.", messages.get(1));
        assertEquals("A Trip is Beginning.", messages.get(2));
        assertEquals("We have received Apples.", messages.get(3));
        assertEquals("A Bus Stop is Requested.", messages.get(4));
        assertEquals("A Trip is Ending.", messages.get(5));
        assertEquals("The Bus is Stopping.", messages.get(6));
        assertEquals("The Bus has terminated.", messages.get(7));

        assertEquals(8, messages.size());

        Object secondBus = lookup.lookup("second-bus");

        ((Resettable) secondBus).hardReset();

        oddjob.run();

        assertEquals(ParentState.EXCEPTION,
                oddjob.lastStateEvent().getState());

        assertEquals("The Bus is Starting.", messages.get(9));
        assertEquals("A Trip is Beginning.", messages.get(10));
        assertEquals("We have received crash-the-bus.", messages.get(11));
        assertEquals("The Bus has Crashed.", messages.get(12));
        assertEquals("The Bus has terminated.", messages.get(13));

        oddjob.destroy();
    }

    @Test
    public void testCutPasteBusPartInvalidatesBus() throws ArooaParseException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/mega/MegaBusCutTest.xml",
                getClass().getClassLoader()));
        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

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

        assertEquals(ParentState.EXCEPTION,
                oddjob.lastStateEvent().getState());

        oddjob.destroy();
    }

}
