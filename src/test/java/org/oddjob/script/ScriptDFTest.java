/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.script;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.is;

/**
 *
 */
public class ScriptDFTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ScriptDFTest.class);

    DesignInstance design;

    @Before
    public void setUp() {
        logger.debug("========================== {} ===================", getName());
    }

    @Test
    public void testCreate() throws ArooaParseException {

        String xml =
                "<script id='this' name='Test Script' language='JavaScript'" +
                        "  resultVariable='result' resultForState='true' exportAll='true'>" +
                        " <input>" +
                        "  <buffer>" +
                        "println(\"Hello\");" +
                        "  </buffer>" +
                        " </input>" +
                        " <bind>" +
                        "  <value key='fruit' value='apple'/>" +
                        " </bind>" +
                        " <export>" +
                        "  <value key='fruit' value='snack'/>" +
                        " </export>" +
                        " <classLoader>" +
                        "  <value value='${this.class.classLoader}'/>" +
                        " </classLoader>" +
                        "</script>";

        ArooaDescriptor descriptor =
                new OddjobDescriptorFactory().createDescriptor(null);

        DesignParser parser = new DesignParser(
                new StandardArooaSession(descriptor));
        parser.setArooaType(ArooaType.COMPONENT);

        parser.parse(new XMLConfiguration("TEST", xml));

        design = parser.getDesign();

        assertThat(design, Matchers.instanceOf(ScriptDesign.class));

        ScriptJob test = (ScriptJob) OddjobTestHelper.createComponentFromConfiguration(
                design.getArooaContext().getConfigurationNode());

        assertThat(test.getLanguage(), is("JavaScript"));
        assertThat(test.getBind("fruit"), is("apple"));
        assertThat(test.getExport("fruit"), is("snack"));
        assertThat(test.getResultVariable(), is("result"));
        assertThat(test.getClassLoader(), is(ScriptJob.class.getClassLoader()));
        assertThat(test.isResultForState(), is(true));
        assertThat(test.isExportAll(), is(true));
    }

    public static void main(String[] args) throws ArooaParseException {

        ScriptDFTest test = new ScriptDFTest();
        test.testCreate();

        ViewMainHelper view = new ViewMainHelper(test.design);
        view.run();

    }
}
