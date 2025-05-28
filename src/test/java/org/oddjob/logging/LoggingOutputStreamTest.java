/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.util.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class LoggingOutputStreamTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(LoggingOutputStreamTest.class);

    OutputStream test;

    final List<String> text = new ArrayList<>();
    LogLevel level;

    class OurLogEventSink implements LogEventSink {

        public void addEvent(LogLevel level, String line) {
            LoggingOutputStreamTest.this.level = level;
            text.add(line);
        }
    }

    @Before
    public void setUp() {

        logger.debug("--------------- {} -----------------", getName());

        test = new LoggingOutputStream(
                null,
                LogLevel.WARN,
                new OurLogEventSink());

        level = null;
    }

    @Test
    public void testByteArray() throws IOException {
        test.write("Hello\nWorld".getBytes());

        assertEquals(1, text.size());
        assertEquals("Hello\n", text.get(0));

        test.close();

        assertEquals(2, text.size());
        assertEquals("World", text.get(1));
    }

    @Test
    public void testByteArray2() throws IOException {
        test.write("01234Something\nDifferent".getBytes(), 5, 14);

        assertEquals(1, text.size());
        assertEquals("Something\n", text.get(0));

        test.close();

        assertEquals(2, text.size());
        assertEquals("Diff", text.get(1));
    }

    @Test
    public void testAdd() throws IOException {
        class LA implements LogEventSink {
            final String[] expected = {"\n", "x\n"};
            final String[] results = new String[expected.length];
            int count = 0;

            public void addEvent(LogLevel level, String line) {
                results[count++] = line;
            }
        }

        LA la = new LA();

        ByteArrayOutputStream dummy = new ByteArrayOutputStream();
        LoggingOutputStream test = new LoggingOutputStream(dummy,
                LogLevel.DEBUG, la);

        byte[] ba = new byte[]{'\n', 'x', '\n'};

        test.add(ba, 0, ba.length);

        test.close();

        for (int i = 0; i < la.results.length; ++i) {
            assertEquals(la.expected[i], la.results[i]);
        }
    }

    @Test
    public void testWindowsBytes() throws IOException {

        byte[] bytes = {
                104,
                101,
                108,
                108,
                111,
                13,
                10,
                103,
                111,
                111,
                100,
                98,
                121,
                101,
                13,
                10};

        IO.copy(new ByteArrayInputStream(bytes), test);
        test.close();

        assertEquals(2, text.size());

        assertEquals("hello\r\n", text.get(0));
        assertEquals("goodbye\r\n", text.get(1));
    }
}
