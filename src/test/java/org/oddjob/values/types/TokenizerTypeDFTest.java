package org.oddjob.values.types;

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

import java.text.ParseException;

import static org.hamcrest.Matchers.is;

/**
 *
 */
public class TokenizerTypeDFTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(TokenizerTypeDFTest.class);

    @Before
    public void setUp() {
        logger.debug("========================== " + getName() + "===================");
    }

    DesignInstance design;

    @Test
    public void testCreate() throws ArooaParseException, ParseException {

        String xml =
                "<tokenizer text='1|^2|3^|^!^4^'" +
                        "      delimiter='|' quote='^' escape='!' regexp='false'/>";

        ArooaDescriptor descriptor =
                new OddjobDescriptorFactory().createDescriptor(null);

        DesignParser parser = new DesignParser(
                new StandardArooaSession(descriptor));
        parser.setArooaType(ArooaType.VALUE);

        parser.parse(new XMLConfiguration("TEST", xml));

        design = parser.getDesign();

        assertEquals(TokenizerDesign.class, design.getClass());

        TokenizerType test = (TokenizerType) OddjobTestHelper.createValueFromConfiguration(
                design.getArooaContext().getConfigurationNode());

        assertEquals("1|^2|3^|^!^4^", test.getText());
        assertEquals("|", test.getDelimiter());
        assertThat(test.getQuote(), is('^'));
        assertThat(test.getEscape(), is('!'));
        assertEquals(false, test.isRegexp());

        String[] result = test.parse();

        assertEquals("1", result[0]);
        assertEquals("2|3", result[1]);
        assertEquals("^4", result[2]);
    }

    public static void main(String[] args) throws ArooaParseException, ParseException {

        TokenizerTypeDFTest test = new TokenizerTypeDFTest();
        test.testCreate();

        ViewMainHelper view = new ViewMainHelper(test.design);
        view.run();

    }

}
