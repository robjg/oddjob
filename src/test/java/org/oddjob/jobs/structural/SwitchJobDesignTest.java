package org.oddjob.jobs.structural;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.layout.LtMainForm;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.tools.OddjobTestHelper;

import static org.hamcrest.Matchers.is;

public class SwitchJobDesignTest extends OjTestCase {

    DesignInstance design;

    @Test
    public void testCreate() throws ArooaParseException {

        String xml =
                "<switch id='ourSwitch' name='A Test'>" +
                        "<value>" +
                        "  <value value='Some Value'/>" +
                        "</value>" +
                        "<switches>" +
                        " <list>" +
                        "  <values>" +
                        "   <value value='one'/>" +
                        "   <value value='two'/>" +
                        "  </values>" +
                        " </list>" +
                        "</switches>" +
                        "<predicates>" +
                        " <list>" +
                        "  <values>" +
                        "   <comparison" +
                        "            eq='42'" +
                        "            ne='2'" +
                        "            lt='43'" +
                        "            le='42'" +
                        "            gt='41'" +
                        "            ge='42'" +
                        "            null='false'" +
                        "            z='false'" +
                        "                  />" +
                        "  </values>" +
                        " </list>" +
                        "</predicates>" +
                        "<jobs>" +
                        " <echo>First Child</echo>" +
                        "</jobs>" +
                        "</switch>";

        ArooaDescriptor descriptor =
                new OddjobDescriptorFactory().createDescriptor(null);

        DesignParser parser = new DesignParser(
                new StandardArooaSession(descriptor));
        parser.setArooaType(ArooaType.COMPONENT);

        parser.parse(new XMLConfiguration("TEST", xml));

        design = parser.getDesign();

        assertThat(design.getClass().getName(), Matchers.containsString(LtMainForm.class.getName()));

        SwitchJob test = (SwitchJob) OddjobTestHelper.createComponentFromConfiguration(
                design.getArooaContext().getConfigurationNode());

        assertThat(test.getName(), is("A Test"));
        assertThat(test.getValue(), is("Some Value"));
        assertThat(test.getSwitches().length, is(2));
        assertThat(test.getPredicates().length, is(1));
        assertThat(OddjobTestHelper.getChildren(test).length, is(1));
    }

    public static void main(String[] args) throws ArooaParseException {

        SwitchJobDesignTest test = new SwitchJobDesignTest();
        test.testCreate();

        ViewMainHelper view = new ViewMainHelper(test.design);
        view.run();
    }
}
