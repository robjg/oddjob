package org.oddjob;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.io.TeeType;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class OddjobConsoleTest {

    static class Console implements LogListener {
        List<String> lines = new ArrayList<>();

        public synchronized void logEvent(LogEvent logEvent) {
            lines.add(logEvent.getMessage());
        }
    }

    @Test
    public void testLoggingBehaviour() throws IOException, NoConversionAvailableException, ConversionFailedException {


        Console console1 = new Console();

        ByteArrayOutputStream ourOut1 = new ByteArrayOutputStream();

        PrintStream saveOut = System.out;

        System.setOut(new PrintStream(ourOut1) {
            @Override
            public String toString() {
                return "OurOut1";
            }
        });

        OddjobConsole.Close close1 = OddjobConsole.initialise();

        OddjobConsole.console().addListener(console1, LogLevel.DEBUG, -1, 0);

        Logger logger = LoggerFactory.getLogger(OddjobConsoleTest.class);

        System.out.println("Console message 1");

        // This will work in a tests because we use follow on the console appender.
        logger.info("Log message 1");

        // Add another stream

        ByteArrayOutputStream ourOut2 = new ByteArrayOutputStream();

        TeeType teeType = new TeeType();
        teeType.setArooaSession(new StandardArooaSession());
        teeType.setOutputs(0, new ArooaObject(System.out));
        teeType.setOutputs(1, new ArooaObject(ourOut2));

        OutputStream anotherOut = teeType.toOutputStream();

        System.setOut(new PrintStream(anotherOut) {
            @Override
            public String toString() {
                return "AnotherOut";
            }
        });

        System.out.println("Console message 2");

        OddjobConsole.Close close2 = OddjobConsole.initialise();

        Console console2 = new Console();

        OddjobConsole.console().addListener(console2, LogLevel.DEBUG, -1, 0);

        System.out.println("Console message 3");

        // Clean up.

        close2.close();

        System.out.println("Console message 4");

        close1.close();

        System.out.println("Console message 5");

        System.setOut(saveOut);

        String consoleText1 = ourOut1.toString();

        logger.info("Console 1 ** >>\n" + consoleText1 + "<< **");

        List<String> consoleLines1 = IOUtils.readLines(new StringReader(consoleText1));

        console1.lines.forEach(l -> logger.info("** {}", l.trim()));

        assertThat(consoleLines1.size(), is(9));

        assertThat(console1.lines.size(), is(7));

        assertThat(console1.lines.get(0).trim(), is("Console message 1"));
        assertThat(console1.lines.get(2).trim(), is("Console message 2"));
        assertThat(console1.lines.get(4).trim(), is("Console message 3"));
        assertThat(console1.lines.get(5).trim(), is("Console message 4"));

        String consoleText2 = ourOut2.toString();

        logger.info("Console 2 ** >>\n" + consoleText2 + "<< **");

        List<String> consoleLines2 = IOUtils.readLines(new StringReader(consoleText2));

        console2.lines.forEach(l -> logger.info("** {}", l.trim()));

        assertThat(consoleLines2.size(), is(5));

        assertThat(console2.lines.size(), is(1));

        assertThat(console2.lines.get(0).trim(), is("Console message 3"));


    }

}
