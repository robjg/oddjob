package org.oddjob.jmx.handlers;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.parsing.QTag;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.remote.things.ConfigOperationInfo;
import org.oddjob.remote.things.ConfigPoint;
import org.oddjob.state.ParentState;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.xmlunit.matchers.CompareMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ConfigPointHandlerFactoryTest {

    @Test
    public void testCopy() throws ArooaConversionException, InterruptedException {

        String xml = "<oddjob>" +
                "<job>" +
                "<sequential>" +
                "<jobs>" +
                "<bean id='mockRemote' class='" + MockRemote.class.getName() + "' " +
                " serverObject='${hello}' parent='${oddjob}'>" +
                " <serverFactory>" +
                "  <bean class='" + ConfigPointHandlerFactory.class.getName() + "' />" +
                " </serverFactory>" +
                " <clientFactory>" +
                "  <bean class='" + ConfigPointHandlerFactory.ClientFactory.class.getName() + "' />" +
                " </clientFactory>" +
                "</bean>" +
                "<echo id='hello'>Hello</echo>" +
                "</jobs>" +
                "</sequential>" +
                "</job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setExport("oddjob", new ArooaObject(oddjob));
        oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        ConfigPoint client = lookup.lookup("mockRemote.clientObject", ConfigPoint.class);

        BlockingQueue<ConfigOperationInfo> exchanger = new LinkedBlockingQueue<>();

        client.addConfigurationSupportsConsumer(exchanger::add);

        ConfigOperationInfo supports = exchanger.poll(5, TimeUnit.SECONDS);

        assertThat(supports, notNullValue());

        assertThat(supports.isCopySupported(), is(true));

        String copy = client.copy();

        String expected = "<echo id='hello'>Hello</echo>";

        assertThat(copy, CompareMatcher.isSimilarTo(expected));
    }

    @Test
    public void testCut() throws ArooaConversionException, InterruptedException {

        String xml = "<oddjob>" +
                "<job>" +
                "<sequential id='sequential'>" +
                " <jobs>" +
                "  <bean id='mockRemote' class='" + MockRemote.class.getName() + "' " +
                "        serverObject='${hello}' parent='${oddjob}'>" +
                "   <serverFactory>" +
                "    <bean class='" + ConfigPointHandlerFactory.class.getName() + "' />" +
                "   </serverFactory>" +
                "   <clientFactory>" +
                "    <bean class='" + ConfigPointHandlerFactory.ClientFactory.class.getName() + "' />" +
                "   </clientFactory>" +
                "  </bean>" +
                "  <echo id='hello'>Hello</echo>" +
                " </jobs>" +
                "</sequential>" +
                "</job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setExport("oddjob", new ArooaObject(oddjob));
        oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<StructuralEvent> childRemoved = new ArrayList<>();

        Structural structural = lookup.lookup("sequential", Structural.class);
        structural.addStructuralListener(new StructuralListener() {
            @Override
            public void childAdded(StructuralEvent event) {

            }

            @Override
            public void childRemoved(StructuralEvent event) {
                childRemoved.add(event);
            }
        });

        ConfigPoint client = lookup.lookup("mockRemote.clientObject", ConfigPoint.class);

        BlockingQueue<ConfigOperationInfo> exchanger = new LinkedBlockingQueue<>();

        client.addConfigurationSupportsConsumer(exchanger::add);

        ConfigOperationInfo supports = exchanger.poll(5, TimeUnit.SECONDS);

        assertThat(supports, notNullValue());

        assertThat(supports.isCutSupported(), is(true));
        assertThat(childRemoved.size(), is(0));

        String cut = client.cut();

        assertThat(childRemoved.size(), is(1));
        assertThat(childRemoved.get(0).getIndex(), is(1));

        String expected = "<echo id='hello'>Hello</echo>";

        assertThat(cut, CompareMatcher.isSimilarTo(expected));
    }

    @Test
    public void testPaste() throws ArooaConversionException, ArooaParseException, InterruptedException {

        String xml = "<oddjob>" +
                "<job>" +
                "<sequential id='sequential'>" +
                " <jobs>" +
                "  <bean id='mockRemote' class='" + MockRemote.class.getName() + "' " +
                "        serverObject='${sequential}' parent='${oddjob}'>" +
                "   <serverFactory>" +
                "    <bean class='" + ConfigPointHandlerFactory.class.getName() + "' />" +
                "   </serverFactory>" +
                "   <clientFactory>" +
                "    <bean class='" + ConfigPointHandlerFactory.ClientFactory.class.getName() + "' />" +
                "   </clientFactory>" +
                "  </bean>" +
                " </jobs>" +
                "</sequential>" +
                "</job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setExport("oddjob", new ArooaObject(oddjob));
        oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<StructuralEvent> childAdded = new ArrayList<>();

        Structural structural = lookup.lookup("sequential", Structural.class);
        structural.addStructuralListener(new StructuralListener() {
            @Override
            public void childAdded(StructuralEvent event) {
                childAdded.add(event);
            }

            @Override
            public void childRemoved(StructuralEvent event) {
            }
        });

        ConfigPoint client = lookup.lookup("mockRemote.clientObject", ConfigPoint.class);

        BlockingQueue<ConfigOperationInfo> exchanger = new LinkedBlockingQueue<>();

        client.addConfigurationSupportsConsumer(exchanger::add);

        ConfigOperationInfo supports = exchanger.poll(5, TimeUnit.SECONDS);

        assertThat(supports, notNullValue());

        assertThat(supports.isPasteSupported(), is(true));
        assertThat(childAdded.size(), is(1));

        client.paste(-1, "<echo id='hello'>Hello</echo>");

        assertThat(childAdded.size(), is(2));
        assertThat(childAdded.get(1).getIndex(), is(1));

        Runnable echo = lookup.lookup("hello", Runnable.class);
        echo.run();

        assertThat(lookup.lookup("hello.text"), is("Hello"));
    }

    @Test
    public void testDelete() throws ArooaConversionException, InterruptedException {

        String xml = "<oddjob>" +
                "<job>" +
                "<sequential id='sequential'>" +
                " <jobs>" +
                "  <bean id='mockRemote' class='" + MockRemote.class.getName() + "' " +
                "        serverObject='${hello}' parent='${oddjob}'>" +
                "   <serverFactory>" +
                "    <bean class='" + ConfigPointHandlerFactory.class.getName() + "' />" +
                "   </serverFactory>" +
                "   <clientFactory>" +
                "    <bean class='" + ConfigPointHandlerFactory.ClientFactory.class.getName() + "' />" +
                "   </clientFactory>" +
                "  </bean>" +
                "  <echo id='hello'>Hello</echo>" +
                " </jobs>" +
                "</sequential>" +
                "</job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setExport("oddjob", new ArooaObject(oddjob));
        oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<StructuralEvent> childRemoved = new ArrayList<>();

        Structural structural = lookup.lookup("sequential", Structural.class);
        structural.addStructuralListener(new StructuralListener() {
            @Override
            public void childAdded(StructuralEvent event) {

            }

            @Override
            public void childRemoved(StructuralEvent event) {
                childRemoved.add(event);
            }
        });

        ConfigPoint client = lookup.lookup("mockRemote.clientObject", ConfigPoint.class);

        BlockingQueue<ConfigOperationInfo> exchanger = new LinkedBlockingQueue<>();

        client.addConfigurationSupportsConsumer(exchanger::add);

        ConfigOperationInfo supports = exchanger.poll(5, TimeUnit.SECONDS);

        assertThat(supports, notNullValue());

        assertThat(supports.isCutSupported(), is(true));
        assertThat(childRemoved.size(), is(0));

        client.delete();

        assertThat(childRemoved.size(), is(1));
        assertThat(childRemoved.get(0).getIndex(), is(1));
    }

    @Test
    public void testPossibleChildren() throws ArooaConversionException, InterruptedException {

        String xml = "<oddjob>" +
                "<job>" +
                "<sequential id='sequential'>" +
                " <jobs>" +
                "  <bean id='mockRemote' class='" + MockRemote.class.getName() + "' " +
                "        serverObject='${sequential}' parent='${oddjob}'>" +
                "   <serverFactory>" +
                "    <bean class='" + ConfigPointHandlerFactory.class.getName() + "' />" +
                "   </serverFactory>" +
                "   <clientFactory>" +
                "    <bean class='" + ConfigPointHandlerFactory.ClientFactory.class.getName() + "' />" +
                "   </clientFactory>" +
                "  </bean>" +
                " </jobs>" +
                "</sequential>" +
                "</job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setExport("oddjob", new ArooaObject(oddjob));
        oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        ConfigPoint client = lookup.lookup("mockRemote.clientObject", ConfigPoint.class);

        BlockingQueue<ConfigOperationInfo> exchanger = new LinkedBlockingQueue<>();

        client.addConfigurationSupportsConsumer(exchanger::add);

        ConfigOperationInfo supports = exchanger.poll(5, TimeUnit.SECONDS);

        assertThat(supports, notNullValue());

        assertThat(supports.isPasteSupported(), is(true));

        QTag[] possibleChildren = client.possibleChildren();

        assertThat(possibleChildren, hasItemInArray(new QTag("echo")));
    }
}