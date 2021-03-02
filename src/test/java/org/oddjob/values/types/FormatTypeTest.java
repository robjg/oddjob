/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.values.types;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.oddjob.ConverterHelper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.ManualClock;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class FormatTypeTest {
    private static final Logger logger = LoggerFactory.getLogger(FormatTypeTest.class);


    @After
    public void tearDown() throws Exception {
        TimeZone.setDefault(null);
    }

    @Test
    public void testLocalDate() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        Date date = DateHelper.parseDateTime("2005-11-01 22:00");

        FormatType ft = new FormatType();
        ft.setDate(date);
        ft.setFormat("yyyyMMdd");

        ArooaConverter converter =
                new ConverterHelper().getConverter();

        String result = converter.convert(ft, String.class);

        assertThat(result, is("20051101"));
    }

    @Test
    public void testTimeZoneDate() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        Date date = DateHelper.parseDateTime("2005-11-01 22:00", "America/Chicago");
        logger.debug("Date: {}", date);
        FormatType ft = new FormatType();
        ft.setDate(date);
        ft.setFormat("yyyyMMdd");
        ft.setTimeZone("America/Chicago");

        ArooaConverter converter =
                new ConverterHelper().getConverter();

        String result = converter.convert(ft, String.class);

        assertThat(result, is("20051101"));
    }

    @Test
    public void testNumberFormat() throws Exception {

        FormatType ft = new FormatType();
        ft.setNumber(22);
        ft.setFormat("##0.00");

        ArooaConverter converter =
                new ConverterHelper().getConverter();

        String result = converter.convert(ft, String.class);

        assertThat(result, is("22.00"));
    }

    @Test
    public void testInOddjob() throws ArooaConversionException, ParseException {

        File file = new File(getClass().getResource(
                "FormatTypeExample.xml").getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.INCOMPLETE));

        String result = new OddjobLookup(oddjob).lookup("file-check.file",
                String.class);

        assertThat(result, is("Data-20051225-000123.dat"));

        oddjob.destroy();
    }

    @Test
    public void testFormatTimeNowExample() {

        ManualClock ourClock = new ManualClock("2013-01-30 08:17");

        File file = new File(getClass().getResource(
                "FormatTimeNow.xml").getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);
        oddjob.setExport("our-clock", new ArooaObject(ourClock));
        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();
        }

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        console.dump(logger);

        String[] lines = console.getLines();

        String[] expected = OddjobTestHelper.streamToLines(getClass(
        ).getResourceAsStream("FormatTimeNow.txt"));

        for (int i = 0; i < expected.length; ++i) {
            assertThat(lines[i].trim(), Matchers.equalToIgnoringCase(expected[i]));
        }

        assertThat(lines.length, is(1));

        oddjob.destroy();
    }
}
