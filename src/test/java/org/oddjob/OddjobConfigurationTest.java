package org.oddjob;

import org.junit.Test;
import org.oddjob.arooa.*;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.parsing.ParseContext;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.runtime.ConfigurationNode;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.EchoJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test being a ConfigurationOwner.
 *
 * @author rob
 */
public class OddjobConfigurationTest {

    private static final Logger logger = LoggerFactory.getLogger(OddjobConfigurationTest.class);

    @Test
    public void testCopy() throws SAXException, IOException {

        String xml =
                "<oddjob>" +
                        "    <job>" +
                        "        <echo id='simple' text='Hello'/>" +
                        "    </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob.load();

        DragPoint dragPoint = oddjob.provideConfigurationSession().dragPointFor(oddjob);

        String result = dragPoint.copy();

        logger.debug("XML:" + result);

        Diff diff = DiffBuilder.compare(xml)
                .withTest(result).ignoreWhitespace()
                .build();

        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testPaste() throws SAXException, IOException, ArooaParseException {

        String xml =
                "<oddjob/>";

        XMLConfiguration config = new XMLConfiguration("TEST", xml);

        final AtomicReference<String> savedXML = new AtomicReference<String>();
        config.setSaveHandler(new XMLConfiguration.SaveHandler() {
            @Override
            public void acceptXML(String xml) {
                savedXML.set(xml);
            }
        });

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(config);
        oddjob.load();

        DragPoint dragPoint = oddjob.provideConfigurationSession().dragPointFor(oddjob);

        String paste =
                "<echo id='simple'>Hello</echo>";

        DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
        dragPoint.paste(0, paste);
        trn.commit();

        oddjob.provideConfigurationSession().save();

        String expected =
                "<oddjob>" +
                        "    <job>" +
                        "        <echo id='simple'><![CDATA[Hello]]></echo>" +
                        "    </job>" +
                        "</oddjob>";

        String result = savedXML.get();

        logger.debug("XML:" + result);

        Diff diff = DiffBuilder.compare(expected)
                .withTest(result).ignoreWhitespace()
                .build();

        assertFalse(diff.toString(), diff.hasDifferences());
    }

    private class OurConfig implements ArooaConfiguration {

        ConfigurationNode configurationNode = mock(ConfigurationNode.class);

        ParseContext context = mock(ParseContext.class);

        ConfigurationHandle handle = mock(ConfigurationHandle.class);

        {
            when(context.getConfigurationNode()).thenReturn(configurationNode);
            when(context.getParent()).thenReturn(context);
            when(handle.getDocumentContext()).thenReturn(context);
        }

        @Override
        public <P extends ParseContext<P>> ConfigurationHandle<P> parse(final P parentContext)
                throws ArooaParseException {
            return handle;
        }
    }

    /**
     * Tracking down a failure to understand an
     * OddjobExplorer Test for the new action.
     */
    @Test
    public void testConfigurationSession() {

        Oddjob oddjob = new Oddjob();

        oddjob.setConfiguration(new OurConfig());

        oddjob.load();

        ArooaDescriptor descriptor =
                oddjob.provideConfigurationSession().getArooaDescriptor();

        ArooaClass cl = descriptor.getElementMappings().mappingFor(
                new ArooaElement("echo"),
                new InstantiationContext(ArooaType.COMPONENT, null));

        assertEquals(cl, new SimpleArooaClass(EchoJob.class));

    }
}
