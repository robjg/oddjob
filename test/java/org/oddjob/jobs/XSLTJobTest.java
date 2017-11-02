package org.oddjob.jobs;

import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import org.oddjob.io.BufferType;
import org.xml.sax.SAXException;
import org.xmlunit.matchers.CompareMatcher;

public class XSLTJobTest {

	String EOL = System.getProperty("line.separator");
	
    @Test
	public void testIdentity() throws SAXException, IOException {
	
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
				
		assertThat(result.getText(), CompareMatcher.isSimilarTo(xml));
	}
	
	
   @Test
	public void testParmeter() throws SAXException, IOException {
		
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
				
		assertThat(result.getText(), CompareMatcher.isSimilarTo(xml));
	}
}
