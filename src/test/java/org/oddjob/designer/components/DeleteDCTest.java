/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.apache.commons.beanutils.DynaBean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
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

/**
 *
 */
public class DeleteDCTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(DeleteDCTest.class);

    @Rule
    public TestName name = new TestName();

    public String getName() {
        return name.getMethodName();
    }

    @Before
    public void setUp() {
        logger.debug("========================== " + getName() + "===================");
    }

    DesignInstance design;

    @Test
    public void testCreate() throws ArooaParseException {

        String xml =
                "<delete name='Test' reallyRoot='true'" +
                        "        logEvery='10' force='true'" +
                        "        maxErrors='2'>" +
                        " <files>" +
                        "  <files files='b/c/*.foo'/>" +
                        " </files>" +
                        "</delete>";

        ArooaDescriptor descriptor =
                new OddjobDescriptorFactory().createDescriptor(
                        getClass().getClassLoader());

        DesignParser parser = new DesignParser(
                new StandardArooaSession(descriptor));
        parser.setArooaType(ArooaType.COMPONENT);

        parser.parse(new XMLConfiguration("TEST", xml));

        design = parser.getDesign();

        assertEquals(DeleteDesign.class, design.getClass());

        DynaBean test = (DynaBean) OddjobTestHelper.createComponentFromConfiguration(
                design.getArooaContext().getConfigurationNode());

        assertEquals("Test", test.get("name"));
        assertEquals(Boolean.TRUE, test.get("force"));
        assertEquals(Boolean.TRUE, test.get("reallyRoot"));
        assertEquals(10, test.get("logEvery"));
        assertEquals(2, test.get("maxErrors"));
    }

    public static void main(String[] args) throws ArooaParseException {

        DeleteDCTest test = new DeleteDCTest();
        test.testCreate();

        ViewMainHelper view = new ViewMainHelper(test.design);
        view.run();

    }

}
