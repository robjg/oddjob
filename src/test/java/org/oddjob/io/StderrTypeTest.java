package org.oddjob.io;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.FragmentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class StderrTypeTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(StderrTypeTest.class);

    @Before
    public void setUp() throws Exception {
        logger.debug("-------------------  " + getName() + "  --------------");
    }


    String EOL = System.getProperty("line.separator");

    @Test
    public void testStderrInOddjob() {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <copy>" +
                        "     <input>" +
                        "      <buffer>Hello" + EOL + "</buffer>" +
                        "     </input>" +
                        "     <output>" +
                        "      <stderr/>" +
                        "     </output>" +
                        "    </copy>" +
                        "    <copy>" +
                        "     <input>" +
                        "      <buffer>World" + EOL + "</buffer>" +
                        "     </input>" +
                        "     <output>" +
                        "      <stderr/>" +
                        "     </output>" +
                        "    </copy>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";
        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        ConsoleCapture results = new ConsoleCapture();
        try (ConsoleCapture.Close close = results.captureConsole()) {

            oddjob.run();
        }

        oddjob.destroy();

        results.dump(logger);

        String[] lines = results.getLines();

        assertEquals("Hello", lines[0]);
        assertEquals("World", lines[1]);
    }

    @Test
    public void testExample() throws ArooaParseException {

        FragmentHelper helper = new FragmentHelper();

        Runnable copy = (Runnable) helper.createComponentFromResource(
                "org/oddjob/io/StderrTypeExample.xml");

        ConsoleCapture results = new ConsoleCapture();
        try (ConsoleCapture.Close close = results.captureConsole()) {

            copy.run();
        }

        String[] lines = results.getLines();

        assertEquals("It's all going wrong!", lines[0].trim());
        assertEquals(1, lines.length);
    }

    @Test
    public void testConsumer() {

        StderrType test = new StderrType();

        ConsoleCapture results = new ConsoleCapture();
        try (ConsoleCapture.Close close = results.captureConsole()) {

            Consumer<Object> consumer = test.toConsumer();

            consumer.accept("apple");

            MatcherAssert.assertThat(results.getLines(), Matchers.is(new String[]{"apple"}));

            consumer.accept("orange");

            MatcherAssert.assertThat(results.getLines(), Matchers.is(new String[]{"apple", "orange"}));
        }
    }

}
