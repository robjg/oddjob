package org.oddjob.sql;

import org.junit.Test;
import org.oddjob.OjTestCase;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SQLScriptProcessorTest extends OjTestCase {

    String EOL = System.lineSeparator();

    static class SqlCapture implements Consumer<String> {

        List<String> results = new ArrayList<>();

        @Override
        public void accept(String sql) {
            String s = sql.trim();
            if (s.length() > 0) {
                results.add(s);
            }
        }
    }

    @Test
    public void testStandardDelimiter() {

        String script =
                EOL +
                        "create" + EOL +
                        "table" + EOL +
                        "test1;" + EOL +
                        "   " + EOL +
                        "create table test2;" + EOL;

        ScriptParser test = new ScriptParser();

        SqlCapture capture = new SqlCapture();

        test.setInput(new ByteArrayInputStream(script.getBytes()));
        test.setTo(capture);

        test.run();

        assertEquals(2, capture.results.size());

        assertEquals("create table test1", capture.results.get(0));
        assertEquals("create table test2", capture.results.get(1));
    }
}
