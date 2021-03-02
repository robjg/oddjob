package org.oddjob.jobs;

import org.junit.Test;
import org.oddjob.io.BufferType;

import java.io.ByteArrayInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class XSLTJobTest {

    String EOL = System.getProperty("line.separator");

    @Test
    public void testIdentity() {

        String xml =
                "<oddjob>" + EOL +
                        "  <job>" + EOL +
                        "    <echo text='Hello'/>" + EOL +
                        "  </job>" + EOL +
                        "</oddjob>" + EOL;

        BufferType result = new BufferType();
        result.configured();

        XSLTJob test = new XSLTJob();
        test.setStylesheet(getClass().getResourceAsStream("styles.xsl"));
        test.setInput(new ByteArrayInputStream(xml.getBytes()));
        test.setOutput(result.toOutputStream());

        test.run();

        assertThat(result.getText(), isSimilarTo(xml).ignoreWhitespace());
    }

    @Test
    public void testParmeter() {

        String xml =
                "<oddjob>" + EOL +
                        "  <job>" + EOL +
                        "    <echo text='Hello'/>" + EOL +
                        "  </job>" + EOL +
                        "</oddjob>" + EOL;

        BufferType result = new BufferType();
        result.configured();

        XSLTJob test = new XSLTJob();
        test.setStylesheet(getClass().getResourceAsStream(
                "styles-with-param.xsl"));
        test.setInput(new ByteArrayInputStream(xml.getBytes()));
        test.setOutput(result.toOutputStream());
        test.setParameters("text", "Hello");

        test.run();

        assertThat(result.getText(), isSimilarTo(xml).ignoreWhitespace());
    }
}
