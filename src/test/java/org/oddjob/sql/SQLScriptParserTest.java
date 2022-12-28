package org.oddjob.sql;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.io.BufferType;
import org.oddjob.sql.SQLJob.DelimiterType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;

public class SQLScriptParserTest extends OjTestCase {

    @Test
    public void testNoDelimiter() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();

        BufferType buffer = new BufferType();
        buffer.setText("SIMPLE TEXT");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("SIMPLE TEXT"));
    }

    @Test
    public void testOneEmptyLine() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();

        BufferType buffer = new BufferType();
        buffer.setText("LINE ONE\n\nLINE TWO\n");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("LINE ONE", "LINE TWO"));
    }

    @Test
    public void testLotsOfEmptyLines() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();

        BufferType buffer = new BufferType();
        buffer.setText("\n\n\nLINE ONE\n\n\n\nLINE TWO\n\n\n");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("LINE ONE", "LINE TWO"));
    }

    @Test
    public void testWindowsLines() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();

        BufferType buffer = new BufferType();
        buffer.setText("\r\n\r\n\r\nLINE ONE\r\n\r\n\r\n\r\nLINE TWO\r\n\r\n\r\n");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("LINE ONE", "LINE TWO"));
    }

    @Test
    public void testComments() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();

        BufferType buffer = new BufferType();
        buffer.setText("--LINE ONE\n\nLINE TWO\n");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("LINE TWO"));
    }

    @Test
    public void testDefaultDelimited() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();

        BufferType buffer = new BufferType();
        buffer.setText("\nONE;\nTWO;\nTHREE\n");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("ONE", "TWO", "THREE"));
    }

    @Test
    public void testNonRowDelimiterOnSeperateLine() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();
        test.setDelimiter("@");

        BufferType buffer = new BufferType();
        buffer.setText("\nONE\n@\n" +
                "\nTWO@\n" +
                "THREE\n@\n\n\n\n");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("ONE", "TWO", "THREE"));
    }

    @Test
    public void testGoDelimiter() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();
        test.setDelimiter("GO");
        test.setDelimiterType(DelimiterType.ROW);

        BufferType buffer = new BufferType();
        buffer.setText("\nONE\ngo\nTWO\nGO\nTHREE\nGO");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("ONE", "TWO", "THREE"));
    }

    @Test
    public void testGoDelimiterWithBlankLines() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();
        test.setDelimiter("GO");
        test.setDelimiterType(DelimiterType.ROW);

        BufferType buffer = new BufferType();
        buffer.setText("\nONE\n\ngo\nTWO\nGO\n\nTHREE\n\n");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("ONE", "TWO", "THREE"));
    }

    @Test
    public void testGoDelimiterWithBlankLines2() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();
        test.setDelimiter("GO");
        test.setDelimiterType(DelimiterType.ROW);

        BufferType buffer = new BufferType();
        buffer.setText("\nONE\n\ngo\n\nTWO\n\nGO\n\nTHREE\nGO\n\n");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("ONE", "TWO", "THREE"));
    }

    @Test
    public void testGoDelimiterWithMultipleLines() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();
        test.setDelimiter("GO");
        test.setDelimiterType(DelimiterType.ROW);

        BufferType buffer = new BufferType();
        buffer.setText(
                "\n" +
                        "ONE\n" +
                        "BANANA\n" +
                        "go\n" +
                        "\n" +
                        "TWO\n" +
                        "\n" +
                        "BANANAS\n" +
                        "\n" +
                        "GO\n" +
                        "\n" +
                        "THREE\n" +
                        "BANANAS\n" +
                        "FOUR\n" +
                        "GO\n" +
                        "\n");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("ONE BANANA", "TWO  BANANAS", "THREE BANANAS FOUR"));
    }

    @Test
    public void testReplaceProperties() throws IOException {

        List<String> results = new ArrayList<>();

        ScriptParser test = new ScriptParser();
        test.setExpandProperties(true);

        StandardArooaSession session = new StandardArooaSession();
        session.getBeanRegistry().register("stuff", "OK");

        test.setArooaSession(session);

        BufferType buffer = new BufferType();
        buffer.setText("THIS IS ${stuff}");
        buffer.configured();

        test.setInput(buffer.toInputStream());
        test.setTo(results::add);

        test.run();

        assertThat(results, contains("THIS IS OK"));
    }
}
