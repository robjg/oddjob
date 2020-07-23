package org.oddjob.jmx;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.parsing.*;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.xmlunit.matchers.CompareMatcher;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ServerDragTest {

    XMLConfiguration configuration = new XMLConfiguration(
            "TEST", "<oddjob id='apples'>" +
            "<job>" +
            " <echo id='colour'>red</echo>" +
            "</job>" +
            "</oddjob>");

    final AtomicReference<String> savedXML = new AtomicReference<>();

    {
        configuration.setSaveHandler(savedXML::set);
    }

    JMXServerJob server;
    JMXClientJob client;

    ConfigurationOwner remoteOddjob;

    static class OurContext extends MockArooaContext {

        ArooaSession session;

        public OurContext(ArooaSession session) {
            this.session = session;
        }


        @Override
        public RuntimeConfiguration getRuntime() {
            return new MockRuntimeConfiguration() {
                @Override
                public void configure() {
                }
            };
        }

        @Override
        public ArooaSession getSession() {
            return session;
        }
    }

    @Before
    public void setUp() throws Exception {

        ArooaSession serverSession = new OddjobSessionFactory().createSession();

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(configuration);

        ComponentPool serverPool = serverSession.getComponentPool();

        serverPool.registerComponent(
                new ComponentTrinity(
                        oddjob,
                        oddjob,
                        new OurContext(serverSession)),
                "main");

        oddjob.setArooaSession(serverSession);

        oddjob.run();


        server = new JMXServerJob();
        server.setArooaSession(serverSession);
        server.setUrl("service:jmx:rmi://");
        server.setRoot(oddjob);

        server.start();


        ArooaSession clientSession = new StandardArooaSession();

        client = new JMXClientJob();
        client.setConnection(server.getAddress());
        client.setArooaSession(clientSession);

        ComponentPool clientPool = clientSession.getComponentPool();

        clientPool.registerComponent(
                new ComponentTrinity(
                        client,
                        client,
                        new OurContext(clientSession)),
                null);

        client.run();

        remoteOddjob = (ConfigurationOwner) new OddjobLookup(client).lookup("main");
    }

    @After
    public void tearDown() throws Exception {
        client.stop();
        server.stop();
    }

    String EOL = System.getProperty("line.separator");


    @Test
    public void testCutLeaf() throws Exception {

        assertNotNull(remoteOddjob);

        Object toCut = new OddjobLookup(client).lookup("main/colour");
        assertNotNull(toCut);

        DragPoint dragPoint = remoteOddjob.provideConfigurationSession().dragPointFor(toCut);

        DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
        dragPoint.cut();
        trn.commit();

        remoteOddjob.provideConfigurationSession().save();

        String expected = "<oddjob id=\"apples\"/>" + EOL;

        assertThat(savedXML.get(), CompareMatcher.isSimilarTo(expected));
    }

    @Test
    public void testEditRoot() throws Exception {

        assertNotNull(remoteOddjob);

        Object toEdit = new OddjobLookup(client).lookup("main");
        assertNotNull(toEdit);

        ConfigurationSession session = remoteOddjob.provideConfigurationSession();
        DragPoint dragPoint = session.dragPointFor(toEdit);

        XMLArooaParser parser = new XMLArooaParser(session.getArooaDescriptor());

        ConfigurationHandle<SimpleParseContext> handle = parser.parse(dragPoint);

        String replacement =
                "<oddjob id=\"oranges\">" + EOL +
                        "    <job>" + EOL +
                        "        <echo id=\"colour\"><![CDATA[orange]]></echo>" + EOL +
                        "    </job>" + EOL +
                        "</oddjob>" + EOL;

        CutAndPasteSupport.replace(
                handle.getDocumentContext().getParent(),
                handle.getDocumentContext(),
                new XMLConfiguration(
                        "REPLACEMENT", replacement));

        handle.save();

        assertNull(savedXML.get());

        remoteOddjob.provideConfigurationSession().save();

        assertThat(savedXML.get(), CompareMatcher.isSimilarTo(replacement));
    }

    @Test
    public void testPaste() throws Exception {

        testCutLeaf();

        Object pastePoint = new OddjobLookup(client).lookup("main");
        assertNotNull(pastePoint);

        DragPoint dragPoint = remoteOddjob.provideConfigurationSession().dragPointFor(pastePoint);

        String paste =
                "<echo id=\"colour\"><![CDATA[orange]]></echo>";

        DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
        dragPoint.paste(0, paste);
        trn.commit();

        remoteOddjob.provideConfigurationSession().save();

        String expected =
                "<oddjob id=\"apples\">" + EOL +
                        "    <job>" + EOL +
                        "        <echo id=\"colour\"><![CDATA[orange]]></echo>" + EOL +
                        "    </job>" + EOL +
                        "</oddjob>" + EOL;

        assertThat(savedXML.get(), CompareMatcher.isSimilarTo(expected));
    }

    @Test
    public void testFailedPaste() {

        // No Cut!

        Object pastePoint = new OddjobLookup(client).lookup("main");
        assertNotNull(pastePoint);

        DragPoint dragPoint = remoteOddjob.provideConfigurationSession().dragPointFor(pastePoint);

        String paste =
                "<echo id=\"colour\"" +
                        "      text=\"orange\"/>";

        try {
            dragPoint.paste(0, paste);
            fail("Should fail because of two nodes.");
        } catch (Exception e) {
            // expected
        }
    }
}
