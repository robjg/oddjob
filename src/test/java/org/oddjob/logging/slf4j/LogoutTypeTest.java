package org.oddjob.logging.slf4j;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.OurDirs;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.logging.Appender;
import org.oddjob.arooa.logging.AppenderAdapter;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.logging.LoggingEvent;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LogoutTypeTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(LogoutTypeTest.class);

    @Before
    public void setUp() throws Exception {
        logger.debug("-------------------  {}  --------------", getName());
    }


    private static class Results implements Appender {

        List<Object> messages = new ArrayList<>();

        @Override
        public void append(LoggingEvent event) {
            messages.add(event.getMessage());
        }
    }

    String EOL = System.lineSeparator();

    String logName = "org.oddjob.wow.Test";

    @Test
    public void testSimpleOutputStream() throws ArooaConversionException, IOException {

        Results results = new Results();

        LoggerAdapter.appenderAdapterFor(logName)
                .addAppender(results, LoggerAdapter.layoutFor("%m"));

        LogoutType logout = new LogoutType();
        logout.setLogger(logName);

        OutputStream test = logout.toOutputStream();

        test.write(("Hello World." + EOL).getBytes());

        test.close();

        LoggerAdapter.appenderAdapterFor(logName).removeAppender(results);

        assertEquals(1, results.messages.size());
        assertEquals("Hello World.", results.messages.get(0));
    }

    @Test
    public void testSimpleConsumer() {

        Results results = new Results();

        LoggerAdapter.appenderAdapterFor(logName)
                .addAppender(results, LoggerAdapter.layoutFor("%m"));

        LogoutType logout = new LogoutType();
        logout.setLogger(logName);

        Consumer<Object> test = logout.toConsumer();

        test.accept("Hello World.");

        LoggerAdapter.appenderAdapterFor(logName).removeAppender(results);

        assertEquals(1, results.messages.size());
        assertEquals("Hello World.", results.messages.get(0));
    }


    @Test
    public void testLogoutInOddjob() throws ArooaPropertyException, ArooaConversionException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <copy>" +
                        "     <input>" +
                        "      <identify id='hello'>" +
                        "       <value>" +
                        "        <buffer>Hello" + EOL + "</buffer>" +
                        "       </value>" +
                        "      </identify>" +
                        "     </input>" +
                        "     <output>" +
                        "      <logout/>" +
                        "     </output>" +
                        "    </copy>" +
                        "    <copy>" +
                        "     <input>" +
                        "      <buffer>World" + EOL + "</buffer>" +
                        "     </input>" +
                        "     <output>" +
                        "      <logout logger='" + logName + "'/>" +
                        "     </output>" +
                        "    </copy>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        Results results = new Results();

        AppenderAdapter appenderAdapter = LoggerAdapter.appenderAdapterFor(logName);
        appenderAdapter.addAppender(results, LoggerAdapter.layoutFor("%m"));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        appenderAdapter.removeAppender(results);

        String sanityCheck = new OddjobLookup(oddjob).lookup("hello", String.class);
        assertEquals("Hello", sanityCheck.trim());

        oddjob.destroy();

        assertTrue(results.messages.get(0).toString().contains("World"));
    }

    @Test
    public void testExample() {

        OurDirs dirs = new OurDirs();

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/logging/slf4j/LogoutExample.xml",
                getClass().getClassLoader()));
        oddjob.setArgs(new String[]{dirs.base().toString()});

        Results results = new Results();

        AppenderAdapter logger = LoggerAdapter.appenderAdapterFor(LogoutType.class);
        logger.addAppender(results, LoggerAdapter.layoutFor("%m"));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        oddjob.destroy();

        MatcherAssert.assertThat(results.messages.get(0).toString().trim(),
                Matchers.is("Test"));

        logger.removeAppender(results);
    }

}
